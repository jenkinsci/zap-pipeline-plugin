package com.vrondakis.zap.workflow;

import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.StepExecution;

public abstract class DefaultStepDescriptorImpl<T extends StepExecution> extends AbstractStepDescriptorImpl {
    private final String functionName;
    private final String displayName;

    public DefaultStepDescriptorImpl(Class<T> clazz, String functionName, String displayName) {
        super(clazz);
        this.functionName = functionName;
        this.displayName = displayName;
    }

    @Override
    public final String getFunctionName() {
        return functionName;
    }

    @Override
    public final String getDisplayName() {
        return displayName;
    }
}