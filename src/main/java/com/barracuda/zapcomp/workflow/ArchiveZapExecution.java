package com.barracuda.zapcomp.workflow;

import com.barracuda.zapcomp.*;
import hudson.*;
import hudson.model.*;
import org.jenkinsci.plugins.workflow.steps.*;

import java.io.*;

/**
 * Executor for archiveZap() function in Jenkinsfile
 */

public class ArchiveZapExecution extends DefaultStepExecutionImpl {
    ArchiveZapExecution(StepContext context) {
        super(context);
    }

    @Override
    public boolean start() {
        try {
            ZapCompare zapCompare = new ZapCompare();

            boolean archiveResult = zapCompare.archiveRawReport(this.build, this.listener);
            if (!archiveResult) {
                listener.getLogger().println("zap-comp: Failed to archive results");
                getContext().onSuccess(true);
                return true;
            }

            if (ZapDriver.getFailBuild() > 0) {
                if (zapCompare.hasNewCriticalAlerts(this.build, this.listener)) {
                    listener.getLogger().println("zap-comp: ZAP detected a new critical alert. Check the ZAP scanning report");
                    build.setResult(Result.FAILURE);
                    getContext().onFailure(
                            new Throwable("zap-comp: ZAP detected a new critical alert. Check the ZAP scanning report"));
                    return false;
                }
            }
        } finally {
            boolean success = ZapDriver.shutdownZap();
            if (!success)
                listener.getLogger().println("zap-comp: Failed to shutdown ZAP (it's not running?)");

        }

        getContext().onSuccess(true);
        return true;
    }
}