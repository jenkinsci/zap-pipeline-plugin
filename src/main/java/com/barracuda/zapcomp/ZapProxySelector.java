package com.barracuda.zapcomp;

import sun.misc.*;
import sun.net.*;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;


public class ZapProxySelector extends ProxySelector {

    final static String[][] props = {
            /*
             * protocol, Property prefix 1, Property prefix 2, ...
             */
            {"http", "http.proxy", "proxy", "socksProxy"},
            {"https", "https.proxy", "proxy", "socksProxy"},
            {"ftp", "ftp.proxy", "ftpProxy", "proxy", "socksProxy"},
            {"gopher", "gopherProxy", "socksProxy"},
            {"socket", "socksProxy"}
    };

    static class NonProxyInfo {
        // Default value for nonProxyHosts, this provides backward compatibility
        // by excluding localhost and its litteral notations.
        static final String defStringVal = "localhost|127.*|[::1]|0.0.0.0|[::0]";

        String hostsSource;
        RegexpPool hostsPool;
        final String property;
        final String defaultVal;
        static NonProxyInfo ftpNonProxyInfo = new NonProxyInfo("ftp.nonProxyHosts", null, null, "");
        static NonProxyInfo httpNonProxyInfo = new NonProxyInfo("http.nonProxyHosts", null, null, "");
        static NonProxyInfo socksNonProxyInfo = new NonProxyInfo("socksNonProxyHosts", null, null, "");

        NonProxyInfo(String p, String s, RegexpPool pool, String d) {
            property = p;
            hostsSource = s;
            hostsPool = pool;
            defaultVal = d;
        }
    }

    @Override
    public java.util.List<Proxy> select(URI uri) {
        System.out.println("select() called on ZapProxySelector");
        if (uri == null) {
            throw new IllegalArgumentException("URI can't be null.");
        }


        System.out.println(uri.toString() + " is uri");
        String protocol = uri.getScheme();
        String host = uri.getHost();

        if (host == null) {
            // This is a hack to ensure backward compatibility in two
            // cases: 1. hostnames contain non-ascii characters,
            // internationalized domain names. in which case, URI will
            // return null, see BugID 4957669; 2. Some hostnames can
            // contain '_' chars even though it's not supposed to be
            // legal, in which case URI will return null for getHost,
            // but not for getAuthority() See BugID 4913253
            String auth = uri.getAuthority();
            if (auth != null) {
                int i;
                i = auth.indexOf('@');
                if (i >= 0) {
                    auth = auth.substring(i+1);
                }
                i = auth.lastIndexOf(':');
                if (i >= 0) {
                    auth = auth.substring(0,i);
                }
                host = auth;
            }
        }

        if (protocol == null || host == null) {
            throw new IllegalArgumentException("protocol = "+protocol+" host = "+host);
        }
        List<Proxy> proxyl = new ArrayList<Proxy>(1);

        NonProxyInfo pinfo = null;

        if ("http".equalsIgnoreCase(protocol)) {
            pinfo = NonProxyInfo.httpNonProxyInfo;
        } else if ("https".equalsIgnoreCase(protocol)) {
            // HTTPS uses the same property as HTTP, for backward
            // compatibility
            pinfo = NonProxyInfo.httpNonProxyInfo;
        } else if ("ftp".equalsIgnoreCase(protocol)) {
            pinfo = NonProxyInfo.ftpNonProxyInfo;
        } else if ("socket".equalsIgnoreCase(protocol)) {
            pinfo = NonProxyInfo.socksNonProxyInfo;
        }

        /**
         * Let's check the System properties for that protocol
         */
        final String proto = protocol;
        final NonProxyInfo nprop = pinfo;
        final String urlhost = host.toLowerCase();

        /**
         * This is one big doPrivileged call, but we're trying to optimize
         * the code as much as possible. Since we're checking quite a few
         * System properties it does help having only 1 call to doPrivileged.
         * Be mindful what you do in here though!
         */
        Proxy p = AccessController.doPrivileged(
                new PrivilegedAction<Proxy>() {
                    public Proxy run() {
                        int i, j;
                        String phost =  null;
                        int pport = 0;
                        String nphosts =  null;
                        InetSocketAddress saddr = null;

                        // Then let's walk the list of protocols in our array
                        for (i=0; i<props.length; i++) {
                            if (props[i][0].equalsIgnoreCase(proto)) {
                                for (j = 1; j < props[i].length; j++) {
                                    /* System.getProp() will give us an empty
                                     * String, "" for a defined but "empty"
                                     * property.
                                     */
                                    phost =  NetProperties.get(props[i][j]+"Host");
                                    if (phost != null && phost.length() != 0)
                                        break;
                                }
                                if (phost == null || phost.length() == 0) {
                                    return Proxy.NO_PROXY;
                                }
                                // If a Proxy Host is defined for that protocol
                                // Let's get the NonProxyHosts property
                                if (nprop != null) {
                                    nphosts = NetProperties.get(nprop.property);
                                    synchronized (nprop) {
                                        if (nphosts == null) {
                                            if (nprop.defaultVal != null) {
                                                nphosts = nprop.defaultVal;
                                            } else {
                                                nprop.hostsSource = null;
                                                nprop.hostsPool = null;
                                            }
                                        }

                                        if (nphosts != null) {
                                            if (!nphosts.equals(nprop.hostsSource)) {
                                                RegexpPool pool = new RegexpPool();
                                                StringTokenizer st = new StringTokenizer(nphosts, "|", false);
                                                try {
                                                    while (st.hasMoreTokens()) {
                                                        pool.add(st.nextToken().toLowerCase(), Boolean.TRUE);
                                                    }
                                                } catch (sun.misc.REException ex) {
                                                }
                                                nprop.hostsPool = pool;
                                                nprop.hostsSource = nphosts;
                                            }
                                        }
                                        if (nprop.hostsPool != null &&
                                                nprop.hostsPool.match(urlhost) != null) {
                                            return Proxy.NO_PROXY;
                                        }
                                    }
                                }
                                // We got a host, let's check for port

                                pport = NetProperties.getInteger(props[i][j]+"Port", 0).intValue();
                                if (pport == 0 && j < (props[i].length - 1)) {
                                    // Can't find a port with same prefix as Host
                                    // AND it's not a SOCKS proxy
                                    // Let's try the other prefixes for that proto
                                    for (int k = 1; k < (props[i].length - 1); k++) {
                                        if ((k != j) && (pport == 0))
                                            pport = NetProperties.getInteger(props[i][k]+"Port", 0).intValue();
                                    }
                                }

                                // Still couldn't find a port, let's use default
                                if (pport == 0) {
                                    if (j == (props[i].length - 1)) // SOCKS
                                        pport = defaultPort("socket");
                                    else
                                        pport = defaultPort(proto);
                                }
                                // We did find a proxy definition.
                                // Let's create the address, but don't resolve it
                                // as this will be done at connection time
                                saddr = InetSocketAddress.createUnresolved(phost, pport);
                                // Socks is *always* the last on the list.
                                if (j == (props[i].length - 1)) {
                                    int version = NetProperties.getInteger("socksProxyVersion", 5).intValue();
                                    return SocksProxy.create(saddr, version);
                                } else {
                                    return new Proxy(Proxy.Type.HTTP, saddr);
                                }
                            }
                        }
                        return Proxy.NO_PROXY;
                    }});

        proxyl.add(p);

        /*
         * If no specific property was set for that URI, we should be
         * returning an iterator to an empty List.
         */
        return proxyl;
    }

    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        if (uri == null || sa == null || ioe == null) {
            throw new IllegalArgumentException("Arguments can't be null.");
        }
        // ignored
    }


    private int defaultPort(String protocol) {
        if ("http".equalsIgnoreCase(protocol)) {
            return 80;
        } else if ("https".equalsIgnoreCase(protocol)) {
            return 443;
        } else if ("ftp".equalsIgnoreCase(protocol)) {
            return 80;
        } else if ("socket".equalsIgnoreCase(protocol)) {
            return 1080;
        } else if ("gopher".equalsIgnoreCase(protocol)) {
            return 80;
        } else {
            return -1;
        }
    }

    private native static boolean init();
    private synchronized native Proxy getSystemProxy(String protocol, String host);
}