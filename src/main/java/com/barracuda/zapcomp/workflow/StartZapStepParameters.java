package com.barracuda.zapcomp.workflow;

import java.util.*;

public class StartZapStepParameters {
    private String host;
    private int port;
    private int timeout;
    private String zapHome;
    private int failBuild;
    private List<String> allowedHosts;
    private String sessionPath;

    public StartZapStepParameters(String host, int port, int timeout, int failBuild, String zapHome, List<String> allowedHosts, String sessionPath) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.zapHome = zapHome;
        this.failBuild = failBuild;
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

    public int getFailBuild() {
        return failBuild;
    }

    public List<String> getAllowedHosts() {
        return allowedHosts;
    }

    public String getSessionPath() {
        return sessionPath;
    }
}