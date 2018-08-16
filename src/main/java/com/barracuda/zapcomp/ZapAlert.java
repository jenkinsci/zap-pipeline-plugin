package com.barracuda.zapcomp;

import org.apache.commons.lang.builder.*;

import java.io.*;
import java.util.*;

/**
 * Each ZAP report has a list of alerts, this is a class to hold a single alert. Used by gson library
 */

public class ZapAlert implements Serializable {
    private String pluginid;
    private String alert;
    private String name;
    private String riskcode;
    private String confidence;
    private String sourceid;
    private String wascid;
    private List<ZapAlertInstance> instances;

    public String getPluginid() {
        return pluginid;
    }

    public void setPluginid(String pluginid) {
        this.pluginid = pluginid;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRiskcode() {
        return riskcode;
    }

    public void setRiskcode(String riskcode) {
        this.riskcode = riskcode;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public List<ZapAlertInstance> getInstances() {
        return instances;
    }

    public void setInstances(List<ZapAlertInstance> instances) {
        this.instances = instances;
    }

    public String getSourceid() {
        return sourceid;
    }

    public void setSourceid(String sourceid) {
        this.sourceid = sourceid;
    }

    public String getWascid() {
        return wascid;
    }

    public void setWascid(String wascid) {
        this.wascid = wascid;
    }

    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || this.getClass() != object.getClass())
            return false;

        ZapAlert alert = (ZapAlert) object;

        return new EqualsBuilder().append(this.pluginid, alert.pluginid).append(this.alert, alert.alert)
                        .append(this.name, alert.name).append(this.riskcode, alert.riskcode)
                        .append(this.confidence, alert.confidence).append(this.sourceid, alert.sourceid)
                        .append(this.wascid, alert.wascid).append(this.instances.hashCode(), alert.instances.hashCode()).isEquals();
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginid, alert, name, riskcode, confidence, sourceid, wascid, instances.hashCode());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("pluginid", this.pluginid).append("alert", this.alert)
                        .append("riskcode", this.riskcode).append("confidence", this.confidence).append("sourceid", this.sourceid)
                        .append("wascid=" + this.wascid).toString();
    }
}
