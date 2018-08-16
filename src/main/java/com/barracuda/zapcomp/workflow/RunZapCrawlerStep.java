package com.barracuda.zapcomp.workflow;

import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.*;

import hudson.*;

import javax.annotation.*;

public class RunZapCrawlerStep extends Step {
    private final RunZapCrawlerParameters zsp;

    /**
     * For runZapCrawler() function in Jenkinsfile
     */

    @DataBoundConstructor
    public RunZapCrawlerStep(@CheckForNull String host) {
        if(host == null){
         host = "localhost";   
        }
        
        this.zsp = new RunZapCrawlerParameters(host);
    }

    @CheckForNull
    public RunZapCrawlerParameters getParameters() {
        return zsp;
    }

    @Override
    public StepExecution start(StepContext context) {
        return new RunZapCrawlerExecution(context, this);
    }

    @Extension
    public static class DescriptorImpl extends DefaultStepDescriptorImpl<RunZapCrawlerExecution> {
        public DescriptorImpl() {
            super(RunZapCrawlerExecution.class, "runZapCrawler", "Run ZAP crawler on a specified host");
        }
    }

}