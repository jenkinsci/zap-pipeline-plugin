package com.vrondakis.zap.workflow;

import java.io.Serializable;

public class ArchiveZapStepParameters implements Serializable {
    private static int DEFAULT_FAIL_ALL = 0;
    private static int DEFAULT_FAIL_HIGH = 1;
    private static int DEFAULT_FAIL_MED = 0;
    private static int DEFAULT_FAIL_LOW = 0;
    private int failAllAlerts;
    private int failHighAlerts;
    private int failMediumAlerts;
    private int failLowAlerts;

    public ArchiveZapStepParameters(Integer failAllAlerts, Integer failHighAlerts, Integer failMediumAlerts,
                                    Integer failLowAlerts) {
        this.failAllAlerts = failAllAlerts == null ? DEFAULT_FAIL_ALL : failAllAlerts;
        this.failHighAlerts = failHighAlerts == null ? DEFAULT_FAIL_HIGH : failHighAlerts;
        this.failMediumAlerts = failMediumAlerts == null ? DEFAULT_FAIL_MED : failMediumAlerts;
        this.failLowAlerts = failLowAlerts == null ? DEFAULT_FAIL_LOW : failLowAlerts;
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
