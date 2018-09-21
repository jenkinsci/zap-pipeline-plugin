package com.barracuda.zapcomp.workflow;

import java.util.*;

public class StartZapStepParameters {
    private String host;
    private int port;
    private int timeout;
    private String zapHome;
    private int failAllAlerts;
    private int failHighAlerts;
    private int failMediumAlerts;
    private int failLowAlerts;
    private List<String> allowedHosts;
    private String sessionPath;

    public StartZapStepParameters(String host, int port, int timeout, int failAllAlerts, int failHighAlerts, int failMediumAlerts, int failLowAlerts, String zapHome, List<String> allowedHosts, String sessionPath) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.zapHome = zapHome;
        this.failAllAlerts = failAllAlerts;
        this.failHighAlerts = failHighAlerts;
        this.failMediumAlerts = failMediumAlerts;
        this.failLowAlerts = failLowAlerts;
        this.allowedHosts = allowedHosts;
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

    public int getFailAllAlerts() {
        return failAllAlerts;
    }

    public int getFailHighAlerts() {
        return failHighAlerts;
    }

    public int getFailMediumAlerts() {
        return failMediumAlerts;
    }

    public int getFailLowAlerts() {
        return failLowAlerts;
    }
}