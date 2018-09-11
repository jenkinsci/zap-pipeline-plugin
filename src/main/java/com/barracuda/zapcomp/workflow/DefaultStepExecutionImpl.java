package com.barracuda.zapcomp.workflow;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.AbstractStepExecutionImpl;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class DefaultStepExecutionImpl extends AbstractStepExecutionImpl {
    protected TaskListener listener;
    protected FilePath ws;
    protected Run build;
    protected Launcher launcher;
    protected Node node;

    DefaultStepExecutionImpl(StepContext context){
        super(context);

        try{
            this.build = context.get(Run.class);
            this.node = context.get(Node.class);
            this.launcher = context.get(Launcher.class);
            this.ws = context.get(FilePath.class);
            this.listener = context.get(TaskListener.class);
        } catch(IOException | InterruptedException e){
            this.listener.getLogger().println("zap-comp: Failed to run: "+e.getClass());
            getContext().onFailure(e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            start();
        } catch (Exception e) {
            this.listener.getLogger().println("zap-comp: Failed to run: "+e.getClass());
        }
    }

    // findbugs fails without this because "non-transient non-serializable instance field in serializable class"
    private void writeObject(ObjectOutputStream out) {
    }

    private void readObject(ObjectInputStream in) {
    }
}
