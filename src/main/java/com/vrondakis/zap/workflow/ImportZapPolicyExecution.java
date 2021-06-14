package com.vrondakis.zap.workflow;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.vrondakis.zap.ZapExecutionException;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import com.vrondakis.zap.ZapDriver;
import com.vrondakis.zap.ZapDriverController;

public class ImportZapPolicyExecution extends DefaultStepExecutionImpl {
    private ImportZapPolicyStepParameters importZapPolicyStepParameters;

    public ImportZapPolicyExecution(StepContext context, ImportZapPolicyStepParameters importZapPolicyStepParameters) {
        super(context);
        this.importZapPolicyStepParameters = importZapPolicyStepParameters;
    }

    @Override
    public boolean start() {
        listener.getLogger().println("zap: Loading attack policy...");
        if (importZapPolicyStepParameters == null || importZapPolicyStepParameters.getPolicyPath().isEmpty()) {
            getContext().onFailure(new ZapExecutionException("Policy path not provided.", listener.getLogger()));
            return false;
        }

        ZapDriver zapDriver = ZapDriverController.getZapDriver(this.run);
        try {
            zapDriver.loadPolicy(importZapPolicyStepParameters.getPolicyPath());
        } catch (Exception e) {
            getContext().onFailure(new ZapExecutionException("Failed to load attack policy at " + importZapPolicyStepParameters.getPolicyPath(), e, listener.getLogger()));
            return false;
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