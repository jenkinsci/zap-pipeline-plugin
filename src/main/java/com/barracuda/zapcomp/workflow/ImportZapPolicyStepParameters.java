package com.barracuda.zapcomp.workflow;

public class ImportZapPolicyStepParameters {
    private String policyPath;

    public ImportZapPolicyStepParameters(String policyPath) {
        this.policyPath = policyPath;
    }

    public String getPolicyPath() {
        return policyPath;
    }
}
