package com.barracuda.zapcomp.workflow;

import hudson.*;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.*;

import javax.annotation.CheckForNull;

public class ImportZapPolicyStep extends Step {
    private final ImportZapPolicyStepParameters zsp;

    @DataBoundConstructor
    public ImportZapPolicyStep(@CheckForNull String policyPath) {
        this.zsp = new ImportZapPolicyStepParameters(policyPath);
    }

    @CheckForNull
    public ImportZapPolicyStepParameters getParameters() {
        return zsp;
    }

    @Override
    public StepExecution start(StepContext context) {
        return new ImportZapPolicyExecution(context, this);
    }

    @Extension
    public static class DescriptorImpl extends DefaultStepDescriptorImpl<ImportZapPolicyExecution> {
        public DescriptorImpl() {
            super(ImportZapPolicyExecution.class, "importZapScanPolicy", "Import a ZAP scan policy from the specified path");
        }
    }
}