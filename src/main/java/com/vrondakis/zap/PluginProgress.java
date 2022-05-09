/* HEADER */
package com.vrondakis.zap;

public class PluginProgress {
    private String name;
    private String version;
    private String revision;
    private String status;
    private String timeExpiredInMs;
    private String requests;
    private String alerts;

    public PluginProgress(String name, String version, String revision, String status, String timeExpiredInMs, String requests, String alerts) {
        this.name = name;
        this.version = version;
        this.revision = revision;
        this.status = status;
        this.timeExpiredInMs = timeExpiredInMs;
        this.requests = requests;
        this.alerts = alerts;
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

    public String getTimeExpiredInMs() {
        return timeExpiredInMs;
    }

    public String getFormattedTimeExpired() {
        if (timeExpiredInMs != null) {
            long totalTimeExpired = Long.parseLong(timeExpiredInMs);
            long minutes = totalTimeExpired / 60000L;
            long seconds = (totalTimeExpired / 1000L) % 60;
            long milliseconds = totalTimeExpired % 1000;
            return String.format("%02d:%02d.%03d", minutes, seconds, milliseconds);
        }
        return "00:00.000";
    }
}
