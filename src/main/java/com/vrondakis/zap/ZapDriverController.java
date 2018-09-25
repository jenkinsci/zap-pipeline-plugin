package com.vrondakis.zap;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import hudson.model.Run;

/**
 * ZapDriver Controls ZAP using HTTP api
 *
 * @see <a href="https://github.com/zaproxy/zaproxy/wiki/ApiDetails">ZAP API Documentation</a>
 */

public class ZapDriverController {
    static final String CMD_DAEMON = "-daemon";
    static final String CMD_HOST = "-host";
    static final String CMD_PORT = "-port";
    static final String CMD_CONFIG = "-config";
    static final String CMD_DISABLEKEY = "api.disablekey=true";
    static final String CMD_REGEX = "api.addrs.addr.regex=true";
    static final String CMD_NAME = "api.addrs.addr.name=10.*";
    static final String CMD_TIMEOUT = "connection.timeoutInSecs=600";

    static final String ZAP_UNIX_PROGRAM = "zap.sh";
    static final String ZAP_WIN_PROGRAM = "zap.bat";

    private static HashMap<String, ZapDriver> zapDrivers = new HashMap<>();

    public static ZapDriver getZapDriver(Run build) {
        ZapDriver driver = zapDrivers.get(build.getUrl());
        if (driver != null)
            return driver;

        return newDriver(build);
    }

    public static ZapDriver newDriver(Run build) {
        ZapDriver driver = new ZapDriver();
        zapDrivers.put(build.getUrl(), driver);

        return driver;
    }

    public static boolean zapDriverExists(Run build) {
        return zapDrivers.containsKey(build.getUrl());
    }

    public static boolean shutdownZap(Run build) {
        boolean success = getZapDriver(build).shutdownZap();
        zapDrivers.remove(build.getUrl());

        return success;
    }

    // Converts map of parameters to URL parameters
    static String formatParams(Map<String, String> params) {
        return params.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("&"));
    }
}
