package com.vrondakis.zap;


import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;

public class ZapActionTest {
    @Rule
    public JenkinsRule rule = new JenkinsRule();

    WorkflowJob job;
    ZapTrendChart chart;

    @Before
    public void setup() throws IOException {
        job = rule.jenkins.createProject(WorkflowJob.class, "zap-project");
        chart = new ZapTrendChart(job);
    }

    @After
    public void cleanup() throws IOException, InterruptedException {
        job.delete();
    }

    @Test
    public void verifyDisplayName() {
        Assert.assertNull(chart.getDisplayName());
    }

    @Test
    public void verifyAction() {
        job.addAction(chart);
        Assert.assertNotNull(job.getAction(ZapTrendChart.class));
    }
}