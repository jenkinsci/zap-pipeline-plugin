package com.vrondakis.zap;

import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public abstract class ZapTests {
    @Rule
    public JenkinsRule rule = new JenkinsRule();

    protected WorkflowJob job;
    protected static TaskListener taskListener;

    @BeforeClass
    public static void setupTests() {
        taskListener = new TaskListenerStub();
    }

    @Before
    public void setup() throws IOException, ExecutionException, InterruptedException {
        job = rule.jenkins.createProject(WorkflowJob.class, "zap-project");
    }

    @After
    public void cleanup() throws IOException, InterruptedException {
        job.delete();
    }
}
