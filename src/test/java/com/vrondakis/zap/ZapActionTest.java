package com.vrondakis.zap;


import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
    public void verifyActionData() {
        // If displayName, iconFileName are not null then there will be an additional button on the sidebar
        Assert.assertNull(chart.getDisplayName());
        Assert.assertNull(chart.getIconFileName());
        Assert.assertEquals(ZapArchive.DIRECTORY_NAME, chart.getUrlName());
    }

    @Test
    public void verifyActionAdded() {
        job.addAction(chart);
        Assert.assertNotNull(job.getAction(ZapTrendChart.class));
    }
}