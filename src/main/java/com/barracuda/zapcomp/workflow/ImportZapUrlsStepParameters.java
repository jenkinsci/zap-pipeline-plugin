package com.barracuda.zapcomp.workflow;

public class LoadZapPolicyStepParameters {
    private String policyName;

    public LoadZapPolicyStepParameters(String policyName) {
        this.policyName = policyName;
    }

    public String getPolicyName() {
        return policyName;
    }
}
