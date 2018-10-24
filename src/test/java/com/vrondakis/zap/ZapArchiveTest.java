package com.vrondakis.zap;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import hudson.FilePath;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class ZapArchiveTest extends ZapTests {
    private WorkflowRun zapRunA;
    private WorkflowRun zapRunB;
    private ZapArchive zapArchiveA;
    private ZapArchive zapArchiveB;
    private FilePath zapDirectoryA;
    private FilePath zapDirectoryB;
    private ZapDriver zapDriver = new ZapDriverStub();


    @Before
    public void setup() throws ExecutionException, InterruptedException, IOException, NullPointerException {
        super.setup();

        // Setup two builds
        zapRunA = job.scheduleBuild2(0).get();
        zapRunB = job.scheduleBuild2(0).get();

        zapArchiveA = new ZapArchive(zapRunA, zapDriver);
        zapArchiveB = new ZapArchive(zapRunB, zapDriver);

        zapDirectoryA = new FilePath(new File(zapRunA.getRootDir() + "/zap"));
        zapDirectoryB = new FilePath(new File(zapRunB.getRootDir() + "/zap"));

        zapDriver.setZapHost("localhost");
        zapDriver.setZapPort(1234);
    }

    @After
    public void cleanup() throws IOException, InterruptedException {
        job.delete();
    }

    @Test
    public void testDirectoryCreation() throws IOException, InterruptedException {
        zapArchiveA.archiveRawReport(zapRunA, job, taskListener, "false-positives.json");

        Assert.assertTrue(zapDirectoryA.isDirectory());
    }

    @Test
    public void testStaticFileSaving() throws IOException, InterruptedException {
        zapArchiveA.archiveRawReport(zapRunA, job, taskListener, "false-positives.json");

        String index = new FilePath(zapDirectoryA, "index.html").readToString();

        FilePath zapIndexFile = new FilePath(new File(Jenkins.getInstance().getPlugin("zap-pipeline").getWrapper().baseResourceURL.getFile(), "index.html"));
        Assert.assertEquals(zapIndexFile.readToString(), index);
    }

    @Test
    public void testAlertCounts() throws IOException, InterruptedException {
        // Archive both of the reports
        zapArchiveA.archiveRawReport(zapRunA, job, taskListener, "false-positives.json");

        // Read alert-count files
        String zapAlertCountRawA = new FilePath(zapDirectoryA, "alert-count.json").readToString();

        Gson gson = new Gson();
        ZapAlertCount zapAlertCountA = gson.fromJson(zapAlertCountRawA, ZapAlertCount.class);

        Assert.assertEquals(zapAlertCountA, new ZapAlertCount(0, 10, 6, 0, zapRunA.getDisplayName()));
    }

    @Test
    public void testAlertCountsFalsePositive() throws IOException, InterruptedException {
        // Save the false positives file for zapRunA, so the alert counts will be different
        saveFalsePositives(zapRunA);

        // Archive the reports
        zapArchiveA.archiveRawReport(zapRunA, job, taskListener, "false-positives.json");

        // Read alert-count files
        String zapAlertCountRawA = new FilePath(zapDirectoryA, "alert-count.json").readToString();

        Gson gson = new Gson();
        ZapAlertCount zapAlertCountA = gson.fromJson(zapAlertCountRawA, ZapAlertCount.class);

        Assert.assertEquals(new ZapAlertCount(0, 8, 6, 2, zapRunA.getDisplayName()), zapAlertCountA);
    }

    @Test
    public void checkPreviousReport() throws IOException, InterruptedException {
        zapArchiveA.archiveRawReport(zapRunA, job, taskListener, "false-positives.json");
        zapArchiveB.archiveRawReport(zapRunB, job, taskListener, "false-positives.json");

        String zapReportA = new FilePath(zapDirectoryA, ZapArchive.RAW_REPORT_FILENAME).readToString();
        String zapReportB = new FilePath(zapDirectoryB, ZapArchive.RAW_OLD_REPORT_FILENAME).readToString();

        Assert.assertNotNull(zapReportB);
        Assert.assertEquals(zapReportB, zapReportA);
    }

    @Test
    public void testActionCreated() {
        // Archive both builds
        zapArchiveA.archiveRawReport(zapRunA, job, taskListener, "false-positives.json");

        // The action should be added on the second build, as it is only added if there is more than one build.
        zapArchiveB.archiveRawReport(zapRunB, job, taskListener, "false-positives.json");
        Assert.assertNotNull(zapRunB.getAction(ZapAction.class));
    }

    @Test
    public void testActionNotCreated() {
        // Archive both builds
        zapArchiveA.archiveRawReport(zapRunA, job, taskListener, "false-positives.json");
        Assert.assertNull(zapRunA.getParent().getAction(ZapAction.class));
    }

    @Test
    public void testFailBuildFalse() throws IOException, InterruptedException {
        saveFalsePositives(zapRunA);
        zapArchiveA.archiveRawReport(zapRunA, job, taskListener, "false-positives.json");

        Assert.assertFalse(zapArchiveA.shouldFailBuild(taskListener));
    }

    @Test
    public void testFailBuild() throws IOException, InterruptedException {
        saveFalsePositives(zapRunA);
        zapArchiveA.archiveRawReport(zapRunA, job, taskListener, "false-positives.json");

        Assert.assertFalse(zapArchiveA.shouldFailBuild(taskListener));
    }

    private void saveFalsePositives(Run<?, ?> run) throws IOException, InterruptedException {
        URL url = Resources.getResource("false-positives.json");

        // Create a false-positives file in one of the workspaces
        String falsePositivesFile = Resources.toString(url, Charsets.UTF_8);
        FilePath fp = new FilePath(new FilePath(zapRunA.getRootDir()), "false-positives.json");
        fp.write(falsePositivesFile, "UTF-8");
    }
}