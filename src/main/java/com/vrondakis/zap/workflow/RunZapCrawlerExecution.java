package com.vrondakis.zap.workflow;

import com.vrondakis.zap.ZapDriver;
import com.vrondakis.zap.ZapDriverController;
import com.vrondakis.zap.ZapExecutionException;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Executor for startZap() function in jenkinsfile
 */
public class RunZapCrawlerExecution extends SynchronousNonBlockingStepExecution<Void> {
    private RunZapCrawlerParameters runZapCrawlerParameters;

    public RunZapCrawlerExecution(StepContext context, RunZapCrawlerParameters runZapCrawlerParameters) {
        super(context);
        this.runZapCrawlerParameters = runZapCrawlerParameters;
    }

    @Override
    public Void run() throws Exception {
        TaskListener listener = getContext().get(TaskListener.class);;
        Run<?, ?> run = getContext().get(Run.class);
        if (runZapCrawlerParameters == null || runZapCrawlerParameters.getHost().equals("")) {
            getContext().onFailure(new ZapExecutionException("Could not run ZAP crawler, no host has been provided",  listener.getLogger()));
            return null;
        }

        listener.getLogger().println("zap: Starting crawler on host " + runZapCrawlerParameters.getHost() + "...");
        ZapDriver zapDriver = ZapDriverController.getZapDriver(run, listener.getLogger());

        try {
            zapDriver.startZapCrawler(runZapCrawlerParameters.getHost());
        } catch (Exception e) {
            getContext().onFailure(new ZapExecutionException("Failed to start ZAP crawler on host: " + runZapCrawlerParameters.getHost(), e, listener.getLogger()));
            return null;
        }

        try {
            zapDriver.zapCrawlerSuccess();
        }  catch (Exception e) {
            getContext().onFailure(new ZapExecutionException("ZAP crawler did not complete successfully.", e, listener.getLogger()));
            return null;
        }


        getContext().onSuccess(true);
        return null;
    }

    // findbugs fails without this because "non-transient non-serializable instance field in serializable class"
    private void writeObject(ObjectOutputStream out) {
    }

    private void readObject(ObjectInputStream in) {
    }
}
