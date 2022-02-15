package com.vrondakis.zap.workflow;

import com.vrondakis.zap.ZapDriverController;
import com.vrondakis.zap.ZapDriverStub;
import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
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
                , true));

        run = job.scheduleBuild2(0).get();

        ZapDriverStub zapDriver = (ZapDriverStub) ZapDriverController.getZapDriver(run);
        Assert.assertNull(zapDriver.getZapHost());
        Assert.assertNull(zapDriver.getAllowedHosts());
        Assert.assertEquals(Collections.emptyMap(), zapDriver.getFailBuild());
        Assert.assertEquals(0, zapDriver.getZapPort());
        Assert.assertEquals(0, zapDriver.getZapTimeout());

        jenkinsRule.assertBuildStatus(Result.FAILURE, run);
    }

    @Test
    public void testNoZapHome() throws Exception {
        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     startZap(host: '" + host + "', port:" + port + ")\n"
                + "}"
                , true));

        run = job.scheduleBuild2(0).get();
        ZapDriverStub zapDriver = (ZapDriverStub) ZapDriverController.getZapDriver(run);

        jenkinsRule.assertBuildStatus(Result.FAILURE, run);
    }

    @Test
    public void verifyMinimumParameterTest() throws Exception {
        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     startZap(host: '" + host + "', port:" + port + ", zapHome:'/opt/zap')\n"
                + "}"
                , true));

        run = job.scheduleBuild2(0).get();

        ZapDriverStub zapDriver = (ZapDriverStub) ZapDriverController.getZapDriver(run);
        Assert.assertEquals(host, zapDriver.getZapHost());
        Assert.assertEquals(port, zapDriver.getZapPort());

        jenkinsRule.assertBuildStatus(Result.SUCCESS, run);
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
                , true));


        run = job.scheduleBuild2(0).get();
        ZapDriverStub zapDriver = (ZapDriverStub) ZapDriverController.getZapDriver(run);

        Assert.assertEquals(host, zapDriver.getZapHost());
        Assert.assertEquals(port, zapDriver.getZapPort());
        Assert.assertEquals(timeout, zapDriver.getZapTimeout());
        Assert.assertEquals(sessionPath, zapDriver.getLoadedSessionPath());
        Assert.assertEquals(Collections.singletonList("github.com"), zapDriver.getAllowedHosts());
        Assert.assertEquals(new ArrayList<>(), zapDriver.getAdditionalConfigurations());
        jenkinsRule.assertBuildStatus(Result.SUCCESS, run);
    }

    @Test
    public void verifyParametersTestWithCerts() throws Exception {
        int timeout = 234;
        String sessionPath = "session.session";


        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     startZap(host: '" + host + "',"
                + "     port:" + port + ","
                + "     timeout:" + timeout + ","
                + "     zapHome: '/opt/zap',"
                + "     rootCaFile: '/opt/certs.pem',"
                + "     sessionPath:'" + sessionPath + "',"
                + "     allowedHosts:['github.com'])\n"
                + "}"
                , true));


        run = job.scheduleBuild2(0).get();
        ZapDriverStub zapDriver = (ZapDriverStub) ZapDriverController.getZapDriver(run);

        Assert.assertEquals(host, zapDriver.getZapHost());
        Assert.assertEquals(port, zapDriver.getZapPort());
        Assert.assertEquals(timeout, zapDriver.getZapTimeout());
        Assert.assertEquals(sessionPath, zapDriver.getLoadedSessionPath());
        Assert.assertEquals(Collections.singletonList("github.com"), zapDriver.getAllowedHosts());
        Assert.assertEquals("/opt/certs.pem", zapDriver.getZapRootCaFile());

        jenkinsRule.assertBuildStatus(Result.SUCCESS, run);
    }

    @Test
    public void verifyParametersTestWithExtraConfiguration() throws Exception {
        int timeout = 234;
        String sessionPath = "session.session";


        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     startZap(host: '" + host + "',"
                + "     port:" + port + ","
                + "     timeout:" + timeout + ","
                + "     zapHome: '/opt/zap',"
                + "     additionalConfigurations: ['connection.proxyChain.enabled=true', 'connection.proxyChain.authEnabled=true'],"
                + "     sessionPath:'" + sessionPath + "',"
                + "     allowedHosts:['github.com'])\n"
                + "}"
                , true));


        run = job.scheduleBuild2(0).get();
        ZapDriverStub zapDriver = (ZapDriverStub) ZapDriverController.getZapDriver(run);

        Assert.assertEquals(host, zapDriver.getZapHost());
        Assert.assertEquals(port, zapDriver.getZapPort());
        Assert.assertEquals(timeout, zapDriver.getZapTimeout());
        Assert.assertEquals(sessionPath, zapDriver.getLoadedSessionPath());
        Assert.assertEquals(Collections.singletonList("github.com"), zapDriver.getAllowedHosts());
        Assert.assertEquals(2, zapDriver.getAdditionalConfigurations().size());
        Assert.assertEquals("connection.proxyChain.enabled=true", zapDriver.getAdditionalConfigurations().get(0));

        jenkinsRule.assertBuildStatus(Result.SUCCESS, run);
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
                , true));


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

        jenkinsRule.assertBuildStatus(Result.SUCCESS, run);
    }
}