package com.vrondakis.zap;

import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ZapDriverControllerTest extends ZapTests {

    private WorkflowRun run;

    @Before
    public void setup() throws IOException, ExecutionException, InterruptedException {
        super.setup();
        run = job.scheduleBuild2(0).get();
    }

    @Test
    public void testNewZapDriver() {
        ZapDriver firstDriver = ZapDriverController.newDriver(run, System.out, ZapDriverStub.class);
        ZapDriver secondDriver = ZapDriverController.getZapDriver(run, System.out);

        Assert.assertSame(firstDriver, secondDriver);
    }

    @Test
    public void testDifferentZapDriver() throws ExecutionException, InterruptedException {
        WorkflowRun secondRun = job.scheduleBuild2(0).get();

        ZapDriver firstDriver = ZapDriverController.newDriver(run, System.out, ZapDriverStub.class);
        ZapDriver secondDriver = ZapDriverController.newDriver(secondRun, System.out, ZapDriverStub.class);

        Assert.assertNotSame(firstDriver, secondDriver);
    }

    @Test
    public void testGetNewZapDriver() throws ExecutionException, InterruptedException {
        ZapDriver firstDriver = ZapDriverController.getZapDriver(run, System.out);

        WorkflowRun secondRun = job.scheduleBuild2(0).get();
        ZapDriver secondDriver = ZapDriverController.getZapDriver(secondRun, System.out);

        Assert.assertNotNull(firstDriver);
        Assert.assertNotNull(secondDriver);

        Assert.assertNotSame(firstDriver, secondDriver);
    }
}