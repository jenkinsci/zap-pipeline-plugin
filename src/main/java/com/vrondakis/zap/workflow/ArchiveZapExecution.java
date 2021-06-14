package com.vrondakis.zap.workflow;

import com.vrondakis.zap.ZapExecutionException;
import com.vrondakis.zap.ZapFailBuildAction;
import hudson.FilePath;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import com.vrondakis.zap.ZapArchive;
import com.vrondakis.zap.ZapDriver;
import com.vrondakis.zap.ZapDriverController;

import hudson.model.Result;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Executor for archiveZap() function in Jenkinsfile
 */

public class ArchiveZapExecution extends DefaultStepExecutionImpl {
    private ArchiveZapStepParameters archiveZapStepParameters;

    public ArchiveZapExecution(StepContext context, ArchiveZapStepParameters parameters) {
        super(context);
        this.archiveZapStepParameters = parameters;
    }

    @Override
    public boolean start() {
        listener.getLogger().println("zap: Archiving results...");
        System.out.println("zap: Archiving results...");
        ZapDriver zapDriver = ZapDriverController.getZapDriver(this.run);

        if (zapDriver.getZapHost() == null && zapDriver.getZapPort() == 0) {
            listener.getLogger().println("zap: Zap does not appear to have been started. Nothing to archive.");
            getContext().onSuccess(true);
            return true;
        }

        try {
            waitUntilPassiveScanHasFinished(zapDriver);
        } catch (Exception e) {
            getContext().onFailure(new ZapExecutionException("Error occurred checking that passive scan to finish.", e, listener.getLogger()));
            getContext().onSuccess(true);
            return false;
        }

        zapDriver.setFailBuild(archiveZapStepParameters.getFailAllAlerts(), archiveZapStepParameters.getFailHighAlerts(),
            archiveZapStepParameters.getFailMediumAlerts(), archiveZapStepParameters.getFailLowAlerts());

        try {
            ZapArchive zapArchive = new ZapArchive(this.run);
            try {
                zapArchive.archiveRawReport(this.run, this.job, this.workspace, this.listener, archiveZapStepParameters.getFalsePositivesFilePath());
            } catch (Exception e) {
                getContext().onFailure(new ZapExecutionException("Failed to archive results.", e, listener.getLogger()));
                return false;
            }

            // If any of the fail run parameters are set to a value more than 1
            if (zapDriver.getFailBuild().values().stream().anyMatch(count -> count > 0)) {
                if (zapArchive.shouldFailBuild(this.listener)) {
                    listener.getLogger().println(
                        "zap: Number of detected ZAP alerts is too high, failing run. Check the ZAP scanning report");
                    run.setResult(Result.FAILURE);
                    getContext().onFailure(new ZapExecutionException("Number of detected ZAP alerts is too high, failing run. Check the ZAP scanning report.", listener.getLogger()));

                    // Red text on build that shows the build has failed due to ZAP
                    this.run.addAction(new ZapFailBuildAction());
                    return false;
                }
            }

        } finally {

            ZapDriver zap = ZapDriverController.getZapDriver(this.run);
            FilePath zapDir = zap.getZapDir();

            try {
                ZapDriverController.shutdownZap(this.run);
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
        return true;
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