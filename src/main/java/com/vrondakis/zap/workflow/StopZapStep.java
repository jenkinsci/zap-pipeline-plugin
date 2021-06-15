package com.vrondakis.zap.workflow;

import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.Serializable;


public class StopZapStep extends Step implements Serializable {
    /**
     * Stop (shutdown) the zap instance.
     */
    @DataBoundConstructor
    public StopZapStep() {}

    @Override
    public StepExecution start(StepContext context) throws IOException, InterruptedException {
        return new StopZapExecution(context);
    }

    @Extension
    public static class DescriptorImpl extends DefaultStepDescriptorImpl<ArchiveZapExecution> {
        public DescriptorImpl() {
            super(ArchiveZapExecution.class, "stopZap", "Stop the ZAP instance.");
        }
    }
}