package com.vrondakis.zap.workflow;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jenkinsci.plugins.workflow.steps.StepContext;

import com.vrondakis.zap.ZapDriver;
import com.vrondakis.zap.ZapDriverController;

public class ImportZapUrlsExecution extends DefaultStepExecutionImpl {
    private ImportZapUrlsStepParameters importZapUrlsStepParameters;

    public ImportZapUrlsExecution(StepContext context, ImportZapUrlsStepParameters importZapUrlsStepParameters) {
        super(context);
        this.importZapUrlsStepParameters = importZapUrlsStepParameters;
    }

    @Override
    public boolean start() {
        listener.getLogger().println("zap: Importing list of URLs...");

        if (importZapUrlsStepParameters == null || importZapUrlsStepParameters.getPath().isEmpty()) {
            getContext().onFailure(new Throwable("zap: Could not load URLs file"));
            return false;
        }

        ZapDriver zapDriver = ZapDriverController.getZapDriver(this.run);

        boolean success = zapDriver.importUrls(importZapUrlsStepParameters.getPath());
        if (!success) {
            listener.getLogger().println("zap: Failed to load list of URLs at " + importZapUrlsStepParameters.getPath());
        }

        getContext().onSuccess(true);
        return true;
    }

    // findbugs fails without this because "non-transient non-serializable instance field in serializable class"
    private void writeObject(ObjectOutputStream out) {
    }

    private void readObject(ObjectInputStream in) {
    }
}
