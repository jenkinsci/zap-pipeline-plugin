package com.barracuda.zapcomp.workflow;

import hudson.*;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.*;

import javax.annotation.CheckForNull;


public class LoadZapPolicyStep extends Step {
    private final LoadZapPolicyStepParameters zsp;

    @DataBoundConstructor
    public LoadZapPolicyStep(@CheckForNull String policyPath) {
        this.zsp = new LoadZapPolicyStepParameters(policyPath);
    }

    @CheckForNull
    public LoadZapPolicyStepParameters getParameters() {
        return zsp;
    }

    @Override
    public StepExecution start(StepContext context) {
        return new LoadZapPolicyExecution(context, this);
    }

    @Extension
    public static class DescriptorImpl extends DefaultStepDescriptorImpl<LoadZapPolicyExecution> {
        public DescriptorImpl() {
            super(LoadZapPolicyExecution.class, "loadZapScanPolicy", "Load a ZAP scan policy from the specified path");
        }
    }


}