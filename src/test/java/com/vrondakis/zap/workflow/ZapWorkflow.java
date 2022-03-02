package com.vrondakis.zap.workflow;

import com.vrondakis.zap.ZapDriverController;
import com.vrondakis.zap.ZapDriverImpl;
import com.vrondakis.zap.ZapDriverStub;
import hudson.slaves.DumbSlave;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;

public abstract class ZapWorkflow {
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    protected WorkflowJob job;
    protected WorkflowRun run;

    @Before
    public void setup() throws Exception {
        job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "zap-pipeline");

        DumbSlave slave = new DumbSlave(
                "slave",
                tmp.newFolder("zap-workspace").getPath(),
                jenkinsRule.createComputerLauncher(null));

        jenkinsRule.jenkins.addNode(slave);

        ZapDriverController.setZapDriverClass(ZapDriverStub.class);
    }

    @After
    public void cleanup() throws IOException, InterruptedException {
        job.delete();
        ZapDriverController.setZapDriverClass(ZapDriverImpl.class);
        ZapDriverController.clearAll();
    }
}
