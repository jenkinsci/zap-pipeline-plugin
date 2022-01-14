package com.vrondakis.zap.workflow;

import java.io.IOException;

import hudson.model.Job;
import org.jenkinsci.plugins.workflow.steps.AbstractStepExecutionImpl;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;

import javax.annotation.Nonnull;

public abstract class DefaultStepExecutionImpl extends AbstractStepExecutionImpl {
    Run<?, ?> run;
    Node node;
    FilePath workspace;
    Launcher launcher;
    TaskListener listener;

    DefaultStepExecutionImpl(StepContext context) {
        super(context);
        try {
            this.run = context.get(Run.class);
            this.node = context.get(Node.class);
            this.launcher = context.get(Launcher.class);
            this.workspace = context.get(FilePath.class);
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

    @Override
    public void stop(@Nonnull Throwable throwable) throws Exception {
        // findbugs
    }
}
