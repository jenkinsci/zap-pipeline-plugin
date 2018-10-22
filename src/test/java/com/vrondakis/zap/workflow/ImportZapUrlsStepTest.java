package com.vrondakis.zap.workflow;

import com.google.common.io.Resources;
import com.vrondakis.zap.ZapDriver;
import com.vrondakis.zap.ZapDriverController;
import com.vrondakis.zap.ZapDriverImpl;
import com.vrondakis.zap.ZapDriverStub;
import hudson.FilePath;
import hudson.model.Descriptor;
import hudson.model.Node;
import hudson.model.Result;
import hudson.model.queue.QueueTaskFuture;
import hudson.slaves.DumbSlave;
import hudson.slaves.NodeProperty;
import hudson.slaves.RetentionStrategy;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;


public class ImportZapUrlsStepTest {
    @Rule
    public JenkinsRule r = new JenkinsRule();

    private WorkflowJob job;
    private WorkflowRun run;

    DumbSlave slave;

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private File testWorkspace;

    @Before
    public void setup() throws IOException, URISyntaxException, Descriptor.FormException {
        job = r.jenkins.createProject(WorkflowJob.class, "zap-pipeline");
        testWorkspace = tmp.newFolder("zap-workspace");

        slave = new DumbSlave("slave", "zap", testWorkspace.getPath(), "1", Node.Mode.NORMAL, "", r.createComputerLauncher(null), RetentionStrategy.NOOP, Collections.<NodeProperty<?>>emptyList());
        r.jenkins.addNode(slave);
    }

    @After
    public void cleanup() throws IOException, InterruptedException {
        job.delete();

        ZapDriverController.setZapDriverClass(ZapDriverImpl.class);
    }

    @Test
    public void testUnknownPathImportUrls() throws Exception, NullPointerException {
        ZapDriverController.setZapDriverClass(ZapDriverStub.class);

        job.setDefinition(new CpsFlowDefinition(""
                + " node('slave') {\n"
                + " importZapUrls(path: " + "\"this-does-not-exist.txt\"" + ")"
                + "}", true));

        QueueTaskFuture<WorkflowRun> runFuture = job.scheduleBuild2(0);
        run = runFuture.get();

        r.assertBuildStatus(Result.FAILURE, run);
    }

    @Test
    public void testNoArgumentImportUrls() throws Exception, NullPointerException {
        ZapDriverController.setZapDriverClass(ZapDriverStub.class);

        job.setDefinition(new CpsFlowDefinition(""
                + " node('slave') {\n"
                + " importZapUrls()"
                + "}", true));

        QueueTaskFuture<WorkflowRun> runFuture = job.scheduleBuild2(0);
        run = runFuture.get();

        r.assertBuildStatus(Result.FAILURE, run);
    }

    @Test
    public void testWithCorrectPath() throws Exception, NullPointerException {
        ZapDriverController.setZapDriverClass(ZapDriverStub.class);

        job.setDefinition(new CpsFlowDefinition(""
                + " node('slave') {\n"
                + " importZapUrls(path: \""+Resources.getResource("urls.txt").getPath()+"\")"
                + "}", true));

        job.scheduleBuild2(0).get();

        r.assertBuildStatus(Result.SUCCESS, run);

    }
}


