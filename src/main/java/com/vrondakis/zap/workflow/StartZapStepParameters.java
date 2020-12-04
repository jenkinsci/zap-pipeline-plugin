package com.vrondakis.zap.workflow;

import java.util.ArrayList;
import java.util.List;

public class StartZapStepParameters {
    private static int DEFAULT_TIMEOUT = 1000;
    private static String DEFAULT_ZAP_HOME = System.getProperty("ZAP_HOME");
    private static List<String> DEFAULT_ALLOWED_HOSTS = new ArrayList<>();
    private String host;
    private String zapDir;
    private int port;
    private int timeout;
    private String zapHome;
    private List<String> allowedHosts;
    private String sessionPath;

    public StartZapStepParameters(String host, int port, int timeout, String zapHome, List<String> allowedHosts,
                                  String sessionPath) {
        this.host = host;
        this.port = port;
        this.timeout = timeout == 0 ? DEFAULT_TIMEOUT : timeout;
        this.zapHome = (zapHome == null || zapHome.isEmpty()) ? DEFAULT_ZAP_HOME : zapHome;
        this.allowedHosts = (allowedHosts == null || allowedHosts.isEmpty()) ? DEFAULT_ALLOWED_HOSTS : allowedHosts;
        this.sessionPath = sessionPath;
    }


    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getTimeout() {
        return timeout;
    }

    public String getZapHome() {
        return zapHome;
    }

    public List<String> getAllowedHosts() {
        return allowedHosts;
    }

    public String getSessionPath() {
        return sessionPath;
    }
}