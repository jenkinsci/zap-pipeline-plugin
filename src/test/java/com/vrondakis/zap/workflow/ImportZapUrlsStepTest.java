package com.vrondakis.zap.workflow;

import com.vrondakis.zap.ZapDriverController;
import com.vrondakis.zap.ZapDriverStub;
import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.junit.Test;


public class ImportZapUrlsStepTest extends ZapWorkflow {
    @Test
    public void testPathImportUrls() throws Exception {
        ZapDriverController.setZapDriverClass(ZapDriverStub.class);

        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     importZapUrls(path: " + "'a-file.txt'" + ")\n"
                + "}"
                , true));

        run = job.scheduleBuild2(0).get();
        jenkinsRule.assertBuildStatus(Result.SUCCESS, run);
    }

    @Test
    public void testNoArgumentImportUrls() throws Exception {
        ZapDriverController.setZapDriverClass(ZapDriverStub.class);

        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     importZapUrls()"
                + "}"
                , true));

        run = job.scheduleBuild2(0).get();
        jenkinsRule.assertBuildStatus(Result.FAILURE, run);
    }
}