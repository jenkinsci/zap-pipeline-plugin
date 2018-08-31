package com.barracuda.zapcomp.workflow;

import com.barracuda.zapcomp.*;
import hudson.model.*;
import org.jenkinsci.plugins.workflow.steps.*;

import java.io.*;
import java.time.*;
import java.util.concurrent.*;


public class LoadZapPolicyExecution extends AbstractStepExecutionImpl {
    private TaskListener listener;
    private LoadZapPolicyStep step;

    public LoadZapPolicyExecution(StepContext context, LoadZapPolicyStep step) {
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
        LoadZapPolicyStepParameters zsp = step.getParameters();
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
