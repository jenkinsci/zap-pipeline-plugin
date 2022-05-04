/* HEADER */
package com.vrondakis.zap;

public class PluginProgress {
    private String name;
    private String version;
    private String revision;
    private String status;
    private String requests;
    private String alerts;
    private String additional;

    public PluginProgress(String name, String version, String revision, String status, String requests, String alerts, String additional) {
        this.name = name;
        this.version = version;
        this.revision = revision;
        this.status = status;
        this.requests = requests;
        this.alerts = alerts;
        this.additional = additional;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getRevision() {
        return revision;
    }

    public String getStatus() {
        return status;
    }

    public String getRequests() {
        return requests;
    }

    public String getAlerts() {
        return alerts;
    }

    public String getAdditional() {
        return additional;
    }
}
