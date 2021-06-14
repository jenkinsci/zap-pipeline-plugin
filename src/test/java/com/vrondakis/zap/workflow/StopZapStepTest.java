package com.vrondakis.zap.workflow;

import com.vrondakis.zap.ZapArchive;
import com.vrondakis.zap.ZapDriverController;
import com.vrondakis.zap.ZapDriverStub;
import com.vrondakis.zap.ZapFailBuildAction;
import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;


public class StopZapStepTest extends ZapWorkflow {
    @Test
    public void zapIsShutdown() throws Exception {
        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     startZap(zapHome: '/', port: 1234, host:'123.123.123.123')\n"
                + "     stopZap()\n"
                + "}"
        ));

        run = job.scheduleBuild2(0).get();
        ZapDriverStub zapDriver = (ZapDriverStub) ZapDriverController.getZapDriver(run);

        r.assertBuildStatus(Result.SUCCESS, run);
        Assert.assertNull(run.getAction(ZapFailBuildAction.class));
        Assert.assertTrue(zapDriver.isZapShutdown());
    }

    @Test
    public void testTempFolderDeletedOnArchive() throws Exception {
        // check temp directory has been deleted after archiving
        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     startZap(zapHome: '/', port: 1234, host:'123.123.123.123')\n"
                + "     stopZap()\n"
                + "}"
        ));

        run = job.scheduleBuild2(0).get();

        ZapDriverStub zapDriver = (ZapDriverStub) ZapDriverController.getZapDriver(run);
        Assert.assertNotNull(zapDriver.getZapDir());
        Assert.assertFalse(zapDriver.getZapDir().exists());

        r.assertBuildStatus(Result.SUCCESS, run);
        Assert.assertNull(run.getAction(ZapFailBuildAction.class));
    }
}