package com.vrondakis.zap.workflow;

import java.io.IOException;

import javax.annotation.CheckForNull;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;

public class RunZapCrawlerStep extends Step {
    private final RunZapCrawlerParameters runZapCrawlerParameters;

    @DataBoundConstructor
    public RunZapCrawlerStep(@CheckForNull String host) {
        this.runZapCrawlerParameters = new RunZapCrawlerParameters(host);
    }

    @Override
    public StepExecution start(StepContext context) throws IOException, InterruptedException {
        return new RunZapCrawlerExecution(context, runZapCrawlerParameters);
    }

    @Extension
    public static class DescriptorImpl extends DefaultStepDescriptorImpl<RunZapCrawlerExecution> {
        public DescriptorImpl() {
            super(RunZapCrawlerExecution.class, "runZapCrawler", "Run ZAP crawler on a specified host");
        }
    }
}