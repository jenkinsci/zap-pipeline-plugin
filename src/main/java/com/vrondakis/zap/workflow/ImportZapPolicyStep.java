package com.vrondakis.zap.workflow;

import java.io.IOException;

import javax.annotation.CheckForNull;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;

public class ImportZapPolicyStep extends Step {
    private final ImportZapPolicyStepParameters importZapPolicyStepParameters;

    @DataBoundConstructor
    public ImportZapPolicyStep(@CheckForNull String policyPath) {
        this.importZapPolicyStepParameters = new ImportZapPolicyStepParameters(policyPath);
    }

    @Override
    public StepExecution start(StepContext context) throws IOException, InterruptedException {
        return new ImportZapPolicyExecution(context, importZapPolicyStepParameters);
    }

    @Extension
    public static class DescriptorImpl extends DefaultStepDescriptorImpl<ImportZapPolicyExecution> {
        public DescriptorImpl() {
            super(ImportZapPolicyExecution.class, "importZapScanPolicy", "Import a ZAP scan policy from the specified path");
        }
    }
}