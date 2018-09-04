package com.barracuda.zapcomp.workflow;

import com.barracuda.zapcomp.ZapDriver;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.AbstractStepExecutionImpl;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class ImportZapUrlsExecution extends AbstractStepExecutionImpl {
    private TaskListener listener;
    private ImportZapUrlsStep step;

    public ImportZapUrlsExecution(StepContext context, ImportZapUrlsStep step) {
        super(context);

        try {
            this.step = step;
            this.listener = context.get(TaskListener.class);
        } catch (IOException | InterruptedException e) {
            getContext().onFailure(e);
        }
    }

    @Override
    public boolean start() {
        listener.getLogger().println("zap-comp: Importing list of URLs...");

        ImportZapUrlsStepParameters zsp = step.getParameters();
        if (zsp == null || zsp.getPath().isEmpty()) {
            getContext().onFailure(new Throwable("zap-comp: Could not load URLs file"));
            return false;
        }

        boolean success = ZapDriver.importUrls(zsp.getPath());
        if (!success) {
            listener.getLogger().println("zap-comp: Failed to load list of URLs at " + zsp.getPath());
        }

        getContext().onSuccess(true);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        start();
    }

    // findbugs fails without this because "non-transient non-serializable instance field in serializable class"
    private void writeObject(ObjectOutputStream out) {
    }

    private void readObject(ObjectInputStream in) {
    }

}
