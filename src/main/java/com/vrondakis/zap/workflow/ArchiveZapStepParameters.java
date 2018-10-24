package com.vrondakis.zap.workflow;

import java.io.Serializable;

public class ArchiveZapStepParameters implements Serializable {
    private static int DEFAULT_FAIL_ALL = 0;
    private static int DEFAULT_FAIL_HIGH = 0;
    private static int DEFAULT_FAIL_MED = 0;
    private static int DEFAULT_FAIL_LOW = 0;
    private static String DEFAULT_FALSE_POSITIVES_FILE_PATH = "zapFalsePositives.json";
    private int failAllAlerts;
    private int failHighAlerts;
    private int failMediumAlerts;
    private int failLowAlerts;
    private String falsePositivesFilePath;

    public ArchiveZapStepParameters(Integer failAllAlerts, Integer failHighAlerts, Integer failMediumAlerts,
                                    Integer failLowAlerts, String falsePositivesFilePath) {
        this.failAllAlerts = failAllAlerts == null ? DEFAULT_FAIL_ALL : failAllAlerts;
        this.failHighAlerts = failHighAlerts == null ? DEFAULT_FAIL_HIGH : failHighAlerts;
        this.failMediumAlerts = failMediumAlerts == null ? DEFAULT_FAIL_MED : failMediumAlerts;
        this.failLowAlerts = failLowAlerts == null ? DEFAULT_FAIL_LOW : failLowAlerts;
        this.failLowAlerts = failLowAlerts == null ? DEFAULT_FAIL_LOW : failLowAlerts;
        this.falsePositivesFilePath = falsePositivesFilePath == null ? DEFAULT_FALSE_POSITIVES_FILE_PATH : falsePositivesFilePath;
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

    public String getFalsePositivesFilePath() {
        return falsePositivesFilePath;
    }
}
