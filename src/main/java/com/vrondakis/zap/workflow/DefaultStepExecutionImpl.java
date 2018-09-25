package com.vrondakis.zap.workflow;

import java.io.IOException;

import org.jenkinsci.plugins.workflow.steps.AbstractStepExecutionImpl;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;

public abstract class DefaultStepExecutionImpl extends AbstractStepExecutionImpl {
    TaskListener listener;
    FilePath ws;
    Run build;
    Launcher launcher;
    Node node;

    DefaultStepExecutionImpl(StepContext context) {
        super(context);
        try {
            this.build = context.get(Run.class);
            this.node = context.get(Node.class);
            this.launcher = context.get(Launcher.class);
            this.ws = context.get(FilePath.class);
            this.listener = context.get(TaskListener.class);
        } catch (IOException | InterruptedException e) {
            this.listener.getLogger().println("zap: Failed to run: " + e.getClass());
            getContext().onFailure(e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            start();
        } catch (Exception e) {
            this.listener.getLogger().println("zap: Failed to run: " + e.getClass());
        }
    }
}
