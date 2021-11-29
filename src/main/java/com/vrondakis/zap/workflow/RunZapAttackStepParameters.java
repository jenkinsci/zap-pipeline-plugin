package com.vrondakis.zap.workflow;

public class RunZapAttackStepParameters {
    private String scanPolicyName;
    private int user;
    private int contextId;

    public RunZapAttackStepParameters(String scanPolicyName, int user, int contextId) {
        this.scanPolicyName = scanPolicyName;
        this.user = user;
        this.contextId = contextId;
    }

    public String getScanPolicyName() {
        return scanPolicyName;
    }

    public int getContextId() {
        return contextId;
    }

    public int getUser() {
        return user;
    }
}