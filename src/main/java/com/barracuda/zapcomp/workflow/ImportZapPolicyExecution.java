package com.barracuda.zapcomp.workflow;

import com.barracuda.zapcomp.*;
import hudson.model.*;
import org.jenkinsci.plugins.workflow.steps.*;

import java.io.*;


public class ImportZapPolicyExecution extends AbstractStepExecutionImpl {
    private TaskListener listener;
    private ImportZapPolicyStep step;

    public ImportZapPolicyExecution(StepContext context, ImportZapPolicyStep step) {
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
        listener.getLogger().println("zap-comp: Loading attack policy...");
        ImportZapPolicyStepParameters zsp = step.getParameters();
        if(zsp == null || zsp.getPolicyPath().isEmpty()){
            getContext().onFailure(new Throwable("zap-comp: Policy path not provided!"));
            return false;
        }

        boolean success = ZapDriver.loadPolicy(zsp.getPolicyPath());
        if (!success) {
            listener.getLogger().println("zap-comp: Failed to load attack policy at " + zsp.getPolicyPath());
            getContext().onFailure(new Throwable("zap-comp: Failed to load attack policy at " + zsp.getPolicyPath()));
            return false;
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
