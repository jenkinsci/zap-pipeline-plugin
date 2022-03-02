package com.vrondakis.zap.workflow;

import com.vrondakis.zap.ZapDriver;
import com.vrondakis.zap.ZapDriverController;
import com.vrondakis.zap.ZapExecutionException;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Executor for startZap() function in jenkinsfile
 */
public class RunZapCrawlerExecution extends DefaultStepExecutionImpl {
    private RunZapCrawlerParameters runZapCrawlerParameters;

    public RunZapCrawlerExecution(StepContext context, RunZapCrawlerParameters runZapCrawlerParameters) {
        super(context);
        this.runZapCrawlerParameters = runZapCrawlerParameters;
    }

    @Override
    public Boolean run() {
        if (runZapCrawlerParameters == null || runZapCrawlerParameters.getHost().equals("")) {
            getContext().onFailure(new ZapExecutionException("Could not run ZAP crawler, no host has been provided",  listener.getLogger()));
            return false;
        }

        listener.getLogger().println("zap: Starting crawler on host " + runZapCrawlerParameters.getHost() + "...");
        ZapDriver zapDriver = ZapDriverController.getZapDriver(this.run, listener.getLogger());

        try {
            zapDriver.startZapCrawler(runZapCrawlerParameters.getHost());
        } catch (Exception e) {
            getContext().onFailure(new ZapExecutionException("Failed to start ZAP crawler on host: " + runZapCrawlerParameters.getHost(), e, listener.getLogger()));
            return false;
        }

        try {
            zapDriver.zapCrawlerSuccess();
        }  catch (Exception e) {
            getContext().onFailure(new ZapExecutionException("ZAP crawler did not complete successfully.", e, listener.getLogger()));
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
