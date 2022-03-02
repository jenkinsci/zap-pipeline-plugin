package com.vrondakis.zap.workflow;

import com.vrondakis.zap.ZapDriverController;
import com.vrondakis.zap.ZapDriverStub;
import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;


public class ConfigurePassiveRulesStepTest extends ZapWorkflow {
    @Test
    public void testEnableRules() throws Exception {
        ZapDriverController.setZapDriverClass(ZapDriverStub.class);

        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     configurePassiveRules(action: 'enablePassiveScanners', ids: [1,2])\n"
                + "}"
                , true));

        run = job.scheduleBuild2(0).get();
        jenkinsRule.assertBuildStatus(Result.SUCCESS, run);
        Assert.assertEquals(((ZapDriverStub)ZapDriverController.getZapDriver(run, System.out)).getPassiveRulesIds(), Arrays.asList(1, 2));
        Assert.assertEquals(((ZapDriverStub)ZapDriverController.getZapDriver(run, System.out)).getPassiveRulesAction(), "enable");
    }

    @Test
    public void testDisableRules() throws Exception {
        ZapDriverController.setZapDriverClass(ZapDriverStub.class);

        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     configurePassiveRules(action: 'disablePassiveScanners', ids: [1,2,3])\n"
                + "}"
                , true));

        run = job.scheduleBuild2(0).get();
        jenkinsRule.assertBuildStatus(Result.SUCCESS, run);
        Assert.assertEquals(((ZapDriverStub)ZapDriverController.getZapDriver(run, System.out)).getPassiveRulesIds(), Arrays.asList(1, 2, 3));
        Assert.assertEquals(((ZapDriverStub)ZapDriverController.getZapDriver(run, System.out)).getPassiveRulesAction(), "disable");
    }

    @Test
    public void testWrongActionName() throws Exception {
        ZapDriverController.setZapDriverClass(ZapDriverStub.class);

        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     configurePassiveRules(action: 'wrongName', ids: [1,2])\n"
                + "}"
            , true));

        run = job.scheduleBuild2(0).get();
        jenkinsRule.assertBuildStatus(Result.FAILURE, run);

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