package com.vrondakis.zap.workflow;

import org.jenkinsci.plugins.workflow.steps.StepContext;

import com.vrondakis.zap.ZapArchiver;
import com.vrondakis.zap.ZapDriver;
import com.vrondakis.zap.ZapDriverController;

import hudson.model.Result;

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
        if (ZapDriverController.zapDriverExists(this.build)) {
            ZapDriver zapDriver = ZapDriverController.getZapDriver(this.build);

            zapDriver.setFailBuild(archiveZapStepParameters.getFailAllAlerts(), archiveZapStepParameters.getFailHighAlerts(),
                archiveZapStepParameters.getFailMediumAlerts(), archiveZapStepParameters.getFailLowAlerts());

            try {
                ZapArchiver zapArchiver = new ZapArchiver();

                boolean archiveResult = zapArchiver.archiveRawReport(this.build, this.listener);
                if (!archiveResult) {
                    listener.getLogger().println("zap: Failed to archive results");
                    getContext().onSuccess(true);
                    return true;
                }

                // If any of the fail build parameters are set to a value more than 1
                if (zapDriver.getFailBuild().values().stream().anyMatch(count -> count > 0)) {
                    if (zapArchiver.shouldFailBuild(this.build, this.listener)) {
                        listener.getLogger().println(
                            "zap: Number of detected ZAP alerts is too high, failing build. Check the ZAP scanning report");
                        build.setResult(Result.FAILURE);
                        getContext().onFailure(new Throwable(
                                        "zap: Number of detected ZAP alerts is too high, failing build. Check the ZAP scanning report"));
                        return false;
                    }
                }
            } finally {
                boolean success = ZapDriverController.shutdownZap(this.build);
                if (!success)
                    listener.getLogger().println("zap: Failed to shutdown ZAP (it's not running?)");

            }
        }

        getContext().onSuccess(true);
        return true;
    }
}