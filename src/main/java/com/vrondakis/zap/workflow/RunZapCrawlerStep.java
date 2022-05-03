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
    /**
     * For zapCrawler() function in Jenkinsfile
     *
     * @param host           The host url to scan.
     * @param maxChildren    Set a limit to the number of children scanned (optional).
     * @param contextName    The ZAP context name to run the attack with when running without a user (optional).
     * @param contextId      The ZAP context ID to run the attack with when running with a user (optional).
     * @param subtreeOnly    Restricts the spider to the host url subtree (optional).
     * @param recurse        Prevents the spider from seeding recursively (optional).
     * @param userId         The ZAP user ID to run the crawl with, loaded from the context (optional).
     */
    @DataBoundConstructor
    public RunZapCrawlerStep(@CheckForNull String host, int maxChildren, String contextName, int contextId, boolean subtreeOnly, boolean recurse, int userId) {
        this.runZapCrawlerParameters = new RunZapCrawlerParameters(host, maxChildren, contextName, contextId, subtreeOnly, recurse, userId);
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