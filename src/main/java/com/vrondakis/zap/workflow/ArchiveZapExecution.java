package com.vrondakis.zap.workflow;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;

import com.vrondakis.zap.ZapArchive;
import com.vrondakis.zap.ZapDriver;
import com.vrondakis.zap.ZapDriverController;
import com.vrondakis.zap.ZapExecutionException;
import com.vrondakis.zap.ZapFailBuildAction;

import hudson.FilePath;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;

/**
 * Executor for archiveZap() function in Jenkins
 */
public class ArchiveZapExecution extends SynchronousNonBlockingStepExecution<Void> {
	
	private ArchiveZapStepParameters archiveZapStepParameters;

    public ArchiveZapExecution(StepContext context, ArchiveZapStepParameters parameters) {
        super(context);
        this.archiveZapStepParameters = parameters;
    }
 
    @Override
	protected Void run() throws Exception {
    	TaskListener listener = getContext().get(TaskListener.class);;
    	Run<?, ?> run = getContext().get(Run.class);
        FilePath workspace = getContext().get(FilePath.class);
        Job<?, ?> job = getContext().get(Job.class);
    	
        listener.getLogger().println("zap: Archiving results...");
        System.out.println("zap: Archiving results...");
        ZapDriver zapDriver = ZapDriverController.getZapDriver(run, listener.getLogger());

        if (zapDriver.getZapHost() == null && zapDriver.getZapPort() == 0) {
            listener.getLogger().println("zap: Zap does not appear to have been started. Nothing to archive.");
            getContext().onSuccess(true);
            return null;
        }

        try {
            waitUntilPassiveScanHasFinished(zapDriver);
        } catch (Exception e) {
            getContext().onFailure(new ZapExecutionException("Error occurred checking that passive scan to finish.", e, listener.getLogger()));
            return null;
        }

        zapDriver.setFailBuild(archiveZapStepParameters.getFailAllAlerts(), archiveZapStepParameters.getFailHighAlerts(),
            archiveZapStepParameters.getFailMediumAlerts(), archiveZapStepParameters.getFailLowAlerts());

        try {
            ZapArchive zapArchive = new ZapArchive(run, listener.getLogger());
            try {
                zapArchive.archiveRawReport(run, job, workspace, listener, archiveZapStepParameters.getFalsePositivesFilePath(), archiveZapStepParameters.isDetailedReport());
            } catch (Exception e) {
                getContext().onFailure(new ZapExecutionException("Failed to archive results.", e, listener.getLogger()));
                return null;
            }

            // If any of the fail run parameters are set to a value more than 1
            if (zapDriver.getFailBuild().values().stream().anyMatch(count -> count > 0)) {
                if (zapArchive.shouldFailBuild(listener)) {
                    listener.getLogger().println(
                        "zap: Number of detected ZAP alerts is too high, failing run. Check the ZAP scanning report");
                    run.setResult(Result.FAILURE);
                    getContext().onFailure(new ZapExecutionException("Number of detected ZAP alerts is too high, failing run. Check the ZAP scanning report.", listener.getLogger()));

                    // Red text on build that shows the build has failed due to ZAP
                    run.addAction(new ZapFailBuildAction());
                    return null;
                }
            }

        } finally {

            ZapDriver zap = ZapDriverController.getZapDriver(run, listener.getLogger());
            FilePath zapDir = zap.getZapDir();

            try {
                if (archiveZapStepParameters.shouldShutdown()) {
                    ZapDriverController.shutdownZap(run, listener.getLogger());
                }
            } catch (Exception e) {
                listener.getLogger().println("zap: Failed to shutdown ZAP. " + e.getMessage());
            }

            if (zapDir != null) {
                try {
                    listener.getLogger().println("zap: Deleting temp directory: " + zapDir.getRemote());
                    zapDir.deleteRecursive();
                } catch (IOException | InterruptedException e) {
                    listener.getLogger().println("zap: Failed to delete temp directory. " + e.getMessage());
                }
            }
        }

        getContext().onSuccess(true);
		return null;
    }

    /**
     * Waits until there are no more records to scan for the passive scanner
     * 
     * @param zapDriver the zap driver
     */
    private void waitUntilPassiveScanHasFinished(ZapDriver zapDriver) throws ZapExecutionException, InterruptedException {
        OffsetDateTime startedTime = OffsetDateTime.now();
        int timeoutSeconds = zapDriver.getZapTimeout();

        while (zapDriver.zapRecordsToScan() > 0) {
            if (OffsetDateTime.now().isAfter(startedTime.plusSeconds(timeoutSeconds))) {
                throw new ZapExecutionException("ZAP Scanner failed to complete within the set timeout of " + timeoutSeconds + " seconds.");
            }
            TimeUnit.SECONDS.sleep(ZapDriver.ZAP_SCAN_SLEEP);
        }
    }

}