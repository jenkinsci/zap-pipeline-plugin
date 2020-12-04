package com.vrondakis.zap.workflow;

import com.vrondakis.zap.ZapArchive;
import com.vrondakis.zap.ZapDriverController;
import com.vrondakis.zap.ZapDriverStub;
import com.vrondakis.zap.ZapFailBuildAction;
import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;


public class ArchiveZapStepTest extends ZapWorkflow {
    @Test
    public void testFailureParametersSet() throws Exception {
        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     archiveZap("
                + "         failAllAlerts: 313,"
                + "         failHighAlerts: 314,"
                + "         failMediumAlerts: 315,"
                + "         failLowAlerts: 316"
                + "     )\n"
                + "}"
        ));

        run = job.scheduleBuild2(0).get();

        ZapDriverStub zapDriver = (ZapDriverStub) ZapDriverController.getZapDriver(run);
        HashMap<Integer, Integer> failConditions = zapDriver.getFailBuild();
        Assert.assertEquals(313, (int) failConditions.get(ZapArchive.ALL_ALERT));
        Assert.assertEquals(314, (int) failConditions.get(ZapArchive.HIGH_ALERT));
        Assert.assertEquals(315, (int) failConditions.get(ZapArchive.MEDIUM_ALERT));
        Assert.assertEquals(316, (int) failConditions.get(ZapArchive.LOW_ALERT));

        r.assertBuildStatus(Result.SUCCESS, run);
    }

    @Test
    public void zapArchiveNoZap() throws Exception {
        // If archiveZap() is ran without call starting startZap first, it should not break the build.
        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     archiveZap()\n"
                + "}"
        ));

        run = job.scheduleBuild2(0).get();
        r.assertBuildStatus(Result.SUCCESS, run);
    }

    @Test
    public void testFailureConditions() throws Exception {
        // Test the build actually fails
        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     startZap(zapHome: '/', port: 1234, host:'123.123.123.123')\n"
                + "     archiveZap("
                + "         failAllAlerts: 0,"
                + "         failHighAlerts: 2,"
                + "         failMediumAlerts: 2,"
                + "         failLowAlerts: 2"
                + "     )\n`"
                + "}"
        ));

        run = job.scheduleBuild2(0).get();
        r.assertBuildStatus(Result.FAILURE, run);
    }

    @Test
    public void testAllFailureCondition() throws Exception {
        // Make sure that the 'failAllAlerts' parameter actually counts the correct amoujnt of alerts
        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     startZap(zapHome: '/', port: 1234, host:'123.123.123.123')\n"
                + "     archiveZap("
                + "         failAllAlerts: 6,"
                + "         failHighAlerts: 0,"
                + "         failMediumAlerts: 0,"
                + "         failLowAlerts: 0"
                + "     )\n"
                + "}"
        ));

        run = job.scheduleBuild2(0).get();
        r.assertBuildStatus(Result.FAILURE, run);
    }

    @Test
    public void testZeroFailureConditions() throws Exception {
        // If all the failure parameters are 0, it should not break the build
        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     startZap(zapHome: '/', port: 1234, host:'123.123.123.123')\n"
                + "     archiveZap("
                + "         failAllAlerts: 0,"
                + "         failHighAlerts: 0,"
                + "         failMediumAlerts: 0,"
                + "         failLowAlerts: 0"
                + "     )\n"
                + "}"
        ));

        run = job.scheduleBuild2(0).get();
        r.assertBuildStatus(Result.SUCCESS, run);
        Assert.assertNull(run.getAction(ZapFailBuildAction.class));
    }

    @Test
    public void testFailureActionAdded() throws Exception {
        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     startZap(zapHome: '/', port: 1234, host:'123.123.123.123')\n"
                + "     archiveZap("
                + "         failAllAlerts: 1"
                + "     )\n"
                + "}"
        ));

        run = job.scheduleBuild2(0).get();
        r.assertBuildStatus(Result.FAILURE, run);
        Assert.assertNotNull(run.getAction(ZapFailBuildAction.class));
    }


    @Test
    public void testTempFolderDeletedOnArchive() throws Exception {
        // check temp directory has been deleted after archiving
        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     startZap(zapHome: '/', port: 1234, host:'123.123.123.123')\n"
                + "     archiveZap("
                + "         failAllAlerts: 0,"
                + "         failHighAlerts: 0,"
                + "         failMediumAlerts: 0,"
                + "         failLowAlerts: 0"
                + "     )\n"
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