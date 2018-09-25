package com.vrondakis.zap.workflow;

public class RunZapAttackStepParameters {
    private String scanPolicyName;
    private int user;

    public RunZapAttackStepParameters(String scanPolicyName, int user) {
        this.scanPolicyName = scanPolicyName;
        this.user = user;
    }

    public String getScanPolicyName() {
        return scanPolicyName;
    }

    public int getUser() {
        return user;
    }
}