package com.vrondakis.zap;

/**
 * Used for generating alert-count.json for the graph
 */

class ZapAlertCount {
    private int highAlerts;
    private int mediumAlerts;
    private int lowAlerts;
    private int falsePositives;
    private String buildName;

    boolean hasValues(){
        return !(this.highAlerts <= 0 && this.mediumAlerts <= 0 && this.lowAlerts <= 0 && this.falsePositives <= 0);
    }

    void incrementHigh(){
        highAlerts++;
    }

    void incrementMedium(){
        mediumAlerts++;
    }

    void incrementLow(){
        lowAlerts++;
    }

    void incrementFalsePositives(int count){
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

    public void setBuildName(String buildName){
        this.buildName = buildName;
    }

    public String getBuildName(){
        return buildName;
    }
}
