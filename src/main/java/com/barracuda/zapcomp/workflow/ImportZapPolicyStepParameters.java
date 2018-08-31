package com.barracuda.zapcomp.workflow;

public class LoadZapPolicyStepParameters {
    private String policyPath;

    public LoadZapPolicyStepParameters(String policyPath) {
        this.policyPath = policyPath;
    }

    public String getPolicyPath() {
        return policyPath;
    }
}
