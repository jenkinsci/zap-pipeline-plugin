package com.vrondakis.zap;

import java.util.Objects;

/**
 * Used for generating alert-count.json for the graph
 */

class ZapAlertCount {
    private int highAlerts;
    private int mediumAlerts;
    private int lowAlerts;
    private int falsePositives;
    private String buildName;

    public ZapAlertCount(int highAlerts, int mediumAlerts, int lowAlerts, int falsePositives, String buildName) {
        this.highAlerts = highAlerts;
        this.mediumAlerts = mediumAlerts;
        this.lowAlerts = lowAlerts;
        this.falsePositives = falsePositives;
        this.buildName = buildName;
    }

    boolean hasValues() {
        return !(this.highAlerts <= 0 && this.mediumAlerts <= 0 && this.lowAlerts <= 0 && this.falsePositives <= 0);
    }

    void incrementHigh(int amount) {
        highAlerts += amount;
    }

    void incrementMedium(int amount) {
        mediumAlerts += amount;
    }

    void incrementLow(int amount) {
        lowAlerts += amount;
    }

    void incrementFalsePositives(int count) {
        falsePositives += count;
    }

    // Needed for json conversion
    public int getFalsePositives() {
        return falsePositives;
    }

    public void setFalsePositives(int falsePositives) {
        this.falsePositives = falsePositives;
    }

    public int getLowAlerts() {
        return lowAlerts;
    }

    public void setLowAlerts(int lowAlerts) {
        this.lowAlerts = lowAlerts;
    }

    public int getMediumAlerts() {
        return mediumAlerts;
    }

    public void setMediumAlerts(int mediumAlerts) {
        this.mediumAlerts = mediumAlerts;
    }

    public int getHighAlerts() {
        return highAlerts;
    }

    public void setHighAlerts(int highAlerts) {
        this.highAlerts = highAlerts;
    }

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    public String getBuildName() {
        return buildName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZapAlertCount that = (ZapAlertCount) o;
        return highAlerts == that.highAlerts &&
                mediumAlerts == that.mediumAlerts &&
                lowAlerts == that.lowAlerts &&
                falsePositives == that.falsePositives &&
                Objects.equals(buildName, that.buildName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(highAlerts, mediumAlerts, lowAlerts, falsePositives, buildName);
    }
}
