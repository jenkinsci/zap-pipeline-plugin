package com.vrondakis.zap;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * ZapFalsePositiveInstance The false positive identifier for a single {@code AlertZapInstance} that includes alert type details.
 */

public class ZapFalsePositiveInstance extends ZapAlertInstance {
    private String name;
    private String cweid;
    private String wascid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCweid() {
        return cweid;
    }

    public void setCweid(String cweid) {
        this.cweid = cweid;
    }

    public String getWascid() {
        return wascid;
    }

    public void setWascid(String wascid) {
        this.wascid = wascid;
    }

    public boolean matches(ZapAlert alert, ZapAlertInstance instance) {
        // Match URL as regex
        Pattern urlPattern = this.getUri() != null ? Pattern.compile(this.getUri()) : null;
        if (urlPattern != null) {
            Matcher matcher = urlPattern.matcher(instance.getUri());
            if (!matcher.find()) {
                return false;
            }
        }

        return (this.getName() == null || this.getName().equals(alert.getName()))
                && (this.getCweid() == null || this.getCweid().equals(alert.getCweid()))
                && (this.getWascid() == null || this.getWascid().equals(alert.getWascid()))
                && (this.getMethod() == null || this.getMethod().equals(instance.getMethod()))
                && (this.getParam() == null || this.getParam().equals(instance.getParam()))
                && (this.getAttack() == null || this.getAttack().equals(instance.getAttack()));
    }

    public boolean equals(Object object) {
        if (object != null && getClass() == object.getClass()) {
            ZapFalsePositiveInstance instance = (ZapFalsePositiveInstance) object;
            return new EqualsBuilder().append(getName(), instance.getName()).append(getCweid(), instance.getCweid())
                    .append(getWascid(), instance.getCweid()).append(getUri(), instance.getUri())
                    .append(getMethod(), instance.getMethod()).append(getParam(), instance.getParam()).isEquals();

        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, cweid, wascid, super.hashCode());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("cweid", cweid).append("wascid", wascid)
                .append("uri", getUri()).append("method", getMethod()).append("param", getParam())
                .append("attack", getAttack()).toString();
    }
}