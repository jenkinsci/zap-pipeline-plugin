package com.barracuda.zapcomp.workflow;

import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;


public class ImportZapUrlsStep extends Step {
    private final ImportZapUrlsStepParameters zsp;

    @DataBoundConstructor
    public ImportZapUrlsStep(@CheckForNull String path) {
        this.zsp = new ImportZapUrlsStepParameters(path);
    }

    @CheckForNull
    public ImportZapUrlsStepParameters getParameters() {
        return zsp;
    }

    @Override
    public StepExecution start(StepContext context) {
        return new ImportZapUrlsExecution(context, this);
    }

    @Extension
    public static class DescriptorImpl extends DefaultStepDescriptorImpl<ImportZapUrlsExecution> {
        public DescriptorImpl() {
            super(ImportZapUrlsExecution.class, "importZapUrls", "Load a list of URLs for ZAP to use from the specified path");
        }
    }
}