package com.vrondakis.zap;

import java.lang.reflect.InvocationTargetException;
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
    static final String CMD_DIR = "-dir";
    static final String CMD_CONFIG = "-config";
    static final String CMD_DISABLEKEY = "api.disablekey=true";
    static final String CMD_REGEX = "api.addrs.addr.regex=true";
    static final String CMD_NAME = "api.addrs.addr.name=.*";
    static final String CMD_TIMEOUT = "connection.timeoutInSecs=600";
    static final String CMD_CERTLOAD = "-certload";

    static final String ZAP_UNIX_PROGRAM = "zap.sh";
    static final String ZAP_WIN_PROGRAM = "zap.bat";

    static Class<? extends ZapDriver> zapDriverClass = ZapDriverImpl.class;
    private static HashMap<String, ZapDriver> zapDrivers = new HashMap<>();

    public static ZapDriver getZapDriver(Run run) {
        ZapDriver driver = zapDrivers.get(run.getUrl());
        if (driver != null) {
            return driver;
        }

        System.out.println("zap: Creating new ZAP driver for build URL: " + run.getUrl());
        return newDriver(run);
    }

    public static <T extends ZapDriver> ZapDriver newDriver(Run run, Class<T> zapDriver) {
        ZapDriver zDriver;
        try {
            zDriver = zapDriver.getDeclaredConstructor().newInstance();
            zapDrivers.put(run.getUrl(), zDriver);
            return zDriver;
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ZapDriver newDriver(Run run) {
        return ZapDriverController.newDriver(run, zapDriverClass);
    }

    public static boolean zapDriverExists(Run run) {
        return zapDrivers.containsKey(run.getUrl());
    }

    public static void shutdownZap(Run run) throws ZapExecutionException {
        getZapDriver(run).shutdownZap();

        if (zapDrivers.get(run.getUrl()) instanceof ZapDriverImpl) {
            zapDrivers.remove(run.getUrl());
        }
    }

    // Converts map of parameters to URL parameters
    static String formatParams(Map<String, String> params) {
        return params.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("&"));
    }

    public static <T extends ZapDriver> void setZapDriverClass(Class<T> zapDriver) {
        zapDriverClass = zapDriver;
    }

    public static void clearAll() {
        zapDrivers.clear();
    }
}