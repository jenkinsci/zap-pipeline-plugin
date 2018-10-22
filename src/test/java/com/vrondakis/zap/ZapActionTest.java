package com.vrondakis.zap;


import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

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