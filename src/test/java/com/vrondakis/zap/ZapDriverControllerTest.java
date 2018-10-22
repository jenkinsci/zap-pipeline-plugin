package com.vrondakis.zap;

import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ZapDriverControllerTest {
    private static TaskListener taskListener;
    private WorkflowJob job;
    private WorkflowRun run;

    @Rule
    public JenkinsRule rule = new JenkinsRule();

    @BeforeClass
    public static void setup(){
        taskListener = new TaskListenerStub();
    }

    @Before
    public void setupBuild() throws ExecutionException, InterruptedException, IOException {
        job = rule.jenkins.createProject(WorkflowJob.class, "zap-project");
        run = job.scheduleBuild2(0).get();
    }

    @After
    public void cleanup() throws IOException, InterruptedException {
        job.delete();
    }

    @Test
    public void testNewZapDriver(){
        ZapDriver firstDriver = ZapDriverController.newDriver(run, ZapDriverStub.class);
        ZapDriver secondDriver = ZapDriverController.getZapDriver(run);

        Assert.assertSame(firstDriver, secondDriver);
    }

    @Test
    public void testDifferentZapDriver() throws ExecutionException, InterruptedException {
        WorkflowRun secondRun = job.scheduleBuild2(0).get();

        ZapDriver firstDriver = ZapDriverController.newDriver(run, ZapDriverStub.class);
        ZapDriver secondDriver = ZapDriverController.newDriver(secondRun, ZapDriverStub.class);

        Assert.assertNotSame(firstDriver, secondDriver);
    }

    @Test
    public void testGetNewZapDriver() throws ExecutionException, InterruptedException {
        ZapDriver firstDriver = ZapDriverController.getZapDriver(run);

        WorkflowRun secondRun = job.scheduleBuild2(0).get();
        ZapDriver secondDriver = ZapDriverController.getZapDriver(secondRun);

        Assert.assertNotNull(firstDriver);
        Assert.assertNotNull(secondDriver);

        Assert.assertNotSame(firstDriver, secondDriver);
    }
}
