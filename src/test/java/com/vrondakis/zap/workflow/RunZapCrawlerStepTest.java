package com.vrondakis.zap.workflow;

import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.junit.Test;

public class RunZapCrawlerStepTest extends ZapWorkflow {
    @Test
    public void failNoParameters() throws Exception {
        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     runZapCrawler()\n"
                + "}"
        ));

        run = job.scheduleBuild2(0).get();
        r.assertBuildStatus(Result.FAILURE, run);
    }

    @Test
    public void passWithParametersTest() throws Exception {
        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     runZapCrawler(host: 'http://google.com')\n"
                + "}"
        ));

        run = job.scheduleBuild2(0).get();
        r.assertBuildStatus(Result.SUCCESS, run);
    }
}