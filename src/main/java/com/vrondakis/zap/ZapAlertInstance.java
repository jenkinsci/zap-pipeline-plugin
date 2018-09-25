package com.vrondakis.zap;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * ZapAlertInstance Each ZAP report contains a list of alerts, which have a list of instances where the alerts took place,
 * containing URI, method and evidence.
 */

public class ZapAlertInstance implements Serializable {
    private String uri = "";
    private String method = "";
    private String evidence = "";

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEvidence() {
        return evidence;
    }

    public boolean equals(Object object) {
        if (object instanceof ZapAlertInstance) {
            ZapAlertInstance instance = (ZapAlertInstance) object;
            return new EqualsBuilder().append(this.uri, instance.uri).append(this.method, instance.method)
                            .append(this.evidence, instance.evidence).isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, method, evidence);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("uri", uri).append("method", method).append("evidence", evidence).toString();
    }
}