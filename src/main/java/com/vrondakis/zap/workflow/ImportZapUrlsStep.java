package com.vrondakis.zap.workflow;

import javax.annotation.CheckForNull;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;

public class ImportZapUrlsStep extends Step {
    private final ImportZapUrlsStepParameters importZapUrlsStepParameters;

    @DataBoundConstructor
    public ImportZapUrlsStep(@CheckForNull String path) {
        this.importZapUrlsStepParameters = new ImportZapUrlsStepParameters(path);
    }

    @Override
    public StepExecution start(StepContext context) {
        return new ImportZapUrlsExecution(context, importZapUrlsStepParameters);
    }

    @Extension
    public static class DescriptorImpl extends DefaultStepDescriptorImpl<ImportZapUrlsExecution> {
        public DescriptorImpl() {
            super(ImportZapUrlsExecution.class, "importZapUrls", "Load a list of URLs for ZAP to use from the specified path");
        }
    }
}