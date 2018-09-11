package com.barracuda.zapcomp.workflow;

import javax.annotation.CheckForNull;

public class ImportZapPolicyStepParameters {
    private String policyPath;

    public ImportZapPolicyStepParameters(@CheckForNull String policyPath) {
        this.policyPath = policyPath;
    }

    public String getPolicyPath() {
        return policyPath;
    }
}