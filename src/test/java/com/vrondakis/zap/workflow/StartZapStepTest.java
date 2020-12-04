package com.vrondakis.zap.workflow;

import com.vrondakis.zap.ZapDriverController;
import com.vrondakis.zap.ZapDriverStub;
import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

public class StartZapStepTest extends ZapWorkflow {
    private String host = "127.0.0.2";
    private int port = 1234;

    @Test
    public void testNoArgsError() throws Exception {
        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     startZap()\n"
                + "}"
        ));

        run = job.scheduleBuild2(0).get();

        ZapDriverStub zapDriver = (ZapDriverStub) ZapDriverController.getZapDriver(run);
        Assert.assertNull(zapDriver.getZapHost());
        Assert.assertNull(zapDriver.getAllowedHosts());
        Assert.assertEquals(Collections.emptyMap(), zapDriver.getFailBuild());
        Assert.assertEquals(0, zapDriver.getZapPort());
        Assert.assertEquals(0, zapDriver.getZapTimeout());

        r.assertBuildStatus(Result.FAILURE, run);
    }

    @Test
    public void testNoZapHome() throws Exception {
        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     startZap(host: '" + host + "', port:" + port + ")\n"
                + "}"
        ));

        run = job.scheduleBuild2(0).get();
        ZapDriverStub zapDriver = (ZapDriverStub) ZapDriverController.getZapDriver(run);

        r.assertBuildStatus(Result.FAILURE, run);
    }

    @Test
    public void verifyMinimumParameterTest() throws Exception {
        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     startZap(host: '" + host + "', port:" + port + ", zapHome:'/opt/zap')\n"
                + "}"
        ));

        run = job.scheduleBuild2(0).get();

        ZapDriverStub zapDriver = (ZapDriverStub) ZapDriverController.getZapDriver(run);
        Assert.assertEquals(host, zapDriver.getZapHost());
        Assert.assertEquals(port, zapDriver.getZapPort());

        r.assertBuildStatus(Result.SUCCESS, run);
    }

    @Test
    public void verifyParametersTest() throws Exception {
        int timeout = 234;
        String sessionPath = "session.session";


        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     startZap(host: '" + host + "',"
                + "     port:" + port + ","
                + "     timeout:" + timeout + ","
                + "     zapHome: '/opt/zap',"
                + "     sessionPath:'" + sessionPath + "',"
                + "     allowedHosts:['github.com'])\n"
                + "}"
        ));


        run = job.scheduleBuild2(0).get();
        ZapDriverStub zapDriver = (ZapDriverStub) ZapDriverController.getZapDriver(run);

        Assert.assertEquals(host, zapDriver.getZapHost());
        Assert.assertEquals(port, zapDriver.getZapPort());
        Assert.assertEquals(timeout, zapDriver.getZapTimeout());
        Assert.assertEquals(sessionPath, zapDriver.getLoadedSessionPath());
        Assert.assertEquals(Collections.singletonList("github.com"), zapDriver.getAllowedHosts());

        r.assertBuildStatus(Result.SUCCESS, run);
    }


    @Test
    public void verifyParametersTestWithDir() throws Exception {
        int timeout = 234;
        String sessionPath = "session.session";


        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     startZap(host: '" + host + "',"
                + "     port:" + port + ","
                + "     timeout:" + timeout + ","
                + "     zapHome: '/opt/zap',"
                + "     sessionPath:'" + sessionPath + "',"
                + "     allowedHosts:['github.com'])\n"
                + "}"
        ));


        run = job.scheduleBuild2(0).get();
        ZapDriverStub zapDriver = (ZapDriverStub) ZapDriverController.getZapDriver(run);

        Assert.assertEquals(host, zapDriver.getZapHost());
        Assert.assertEquals(port, zapDriver.getZapPort());
        Assert.assertNotNull(zapDriver.getZapDir());
        Assert.assertTrue(zapDriver.getZapDir().exists());
        Assert.assertTrue(zapDriver.getZapDir().isDirectory());
        Assert.assertEquals(timeout, zapDriver.getZapTimeout());
        Assert.assertEquals(sessionPath, zapDriver.getLoadedSessionPath());
        Assert.assertEquals(Collections.singletonList("github.com"), zapDriver.getAllowedHosts());

        r.assertBuildStatus(Result.SUCCESS, run);

    }
}