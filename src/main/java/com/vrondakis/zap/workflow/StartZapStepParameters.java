package com.vrondakis.zap.workflow;

import java.util.ArrayList;
import java.util.List;

public class StartZapStepParameters {
    private static int DEFAULT_TIMEOUT = 1000;
    private static String DEFAULT_ZAP_HOME = System.getProperty("ZAP_HOME");
    private static List<String> DEFAULT_ALLOWED_HOSTS = new ArrayList<>();
    private static List<String> DEFAULT_ADDITIONAL_CONFIGURATIONS = new ArrayList<>();
    private String host;
    private int port;
    private int timeout;
    private String zapHome;
    private List<String> allowedHosts;
    private List<String> additionalConfigurations;
    private String sessionPath;
    private String rootCaFile;
    private boolean externalZap;

    public StartZapStepParameters(String host, int port, int timeout, String zapHome, List<String> allowedHosts,
                                  String sessionPath, boolean externalZap, String rootCaFile, List<String> additionalConfigurations) {

        this.host = host;
        this.port = port;
        this.timeout = timeout == 0 ? DEFAULT_TIMEOUT : timeout;
        this.zapHome = (zapHome == null || zapHome.isEmpty()) ? DEFAULT_ZAP_HOME : zapHome;
        this.allowedHosts = (allowedHosts == null || allowedHosts.isEmpty()) ? DEFAULT_ALLOWED_HOSTS : allowedHosts;
        this.sessionPath = sessionPath;
        this.rootCaFile = rootCaFile;
        this.additionalConfigurations = (additionalConfigurations == null || additionalConfigurations.isEmpty()) ? DEFAULT_ADDITIONAL_CONFIGURATIONS: additionalConfigurations;
        this.externalZap = externalZap;

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

    public String getRootCaFile() {
        return rootCaFile;
    }

    public List<String> getAdditionalConfigurations() {
        return additionalConfigurations;
    }

    public boolean isExternalZap() {
        return externalZap;
    }

    public void setExternalZap(boolean externalZap) {
        this.externalZap = externalZap;
    }
}