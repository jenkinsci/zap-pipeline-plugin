package com.vrondakis.zap;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;

public class ZapFailBuildActionTest {
    @Rule
    public JenkinsRule rule = new JenkinsRule();

    WorkflowJob job;
    ZapFailBuildAction failBuildAction;

    @Before
    public void setup() throws IOException{
        job = rule.jenkins.createProject(WorkflowJob.class, "zap-project");
        failBuildAction = new ZapFailBuildAction();
    }

    @After
    public void cleanup() throws IOException, InterruptedException{
        job.delete();
    }

    @Test
    public void verifyActionData(){
        Assert.assertNull(failBuildAction.getIconFileName());
        Assert.assertNull(failBuildAction.getDisplayName());
        Assert.assertNull(failBuildAction.getUrlName());
    }

    @Test
    public void verifyActionAdded(){
        job.addAction(failBuildAction);
        Assert.assertNotNull(job.getAction(ZapFailBuildAction.class));
    }
}
