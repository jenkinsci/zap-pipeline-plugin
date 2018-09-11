package com.barracuda.zapcomp.workflow;

import com.barracuda.zapcomp.*;
import hudson.model.*;
import org.jaxen.expr.DefaultStep;
import org.jenkinsci.plugins.workflow.steps.*;

import java.io.*;


public class ImportZapPolicyExecution extends DefaultStepExecutionImpl {
    private ImportZapPolicyStep step;

    public ImportZapPolicyExecution(StepContext context, ImportZapPolicyStep step) {
        super(context);
        this.step = step;
    }

    @Override
    public boolean start() {
        listener.getLogger().println("zap-comp: Loading attack policy...");
        ImportZapPolicyStepParameters zsp = step.getParameters();
        if (zsp == null || zsp.getPolicyPath().isEmpty()) {
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
}