package com.vrondakis.zap;


import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class ZapActionTest {
    @Rule
    public JenkinsRule rule = new JenkinsRule();

    @Test
    public void verifyDisplayName() throws Exception{
        WorkflowJob job;

        job = rule.jenkins.createProject(WorkflowJob.class, "zap-project");
        ZapTrendChart chart = new ZapTrendChart(job);

        Assert.assertNull(chart.getDisplayName());
    }

    @Test
    public void verifyAction() throws Exception{
        WorkflowJob job;
        job = rule.jenkins.createProject(WorkflowJob.class, "zap-project");
        ZapTrendChart chart = new ZapTrendChart(job);
        job.addAction(chart);

        Assert.assertNotNull(job.getAction(ZapTrendChart.class));
    }
}