package com.vrondakis.zap.workflow;

import com.vrondakis.zap.ZapDriver;
import com.vrondakis.zap.ZapDriverController;
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
    public boolean start() {
        if (runZapCrawlerParameters == null || runZapCrawlerParameters.getHost().equals("")) {
            this.listener.getLogger().println("zap: Could not run ZAP crawler, no host has been provided");
            getContext().onSuccess(false);
            return false;
        }

        this.listener.getLogger().println("zap: Starting crawler on host " + runZapCrawlerParameters.getHost() + "...");
        ZapDriver zapDriver = ZapDriverController.getZapDriver(this.run);

        boolean success = zapDriver.startZapCrawler(runZapCrawlerParameters.getHost());
        if (!success) {
            System.out.println("zap: Failed to start ZAP crawler on host " + runZapCrawlerParameters.getHost());
            getContext().onFailure(new Throwable("zap: Failed to start ZAP crawler on host " + runZapCrawlerParameters.getHost()));
            return false;
        }

        boolean completed = zapDriver.zapCrawlerSuccess();
        if (!completed)
            listener.getLogger().println("zap: Could not complete ZAP crawl due to the timeout");


        getContext().onSuccess(true);
        return true;
    }

    // findbugs fails without this because "non-transient non-serializable instance field in serializable class"
    private void writeObject(ObjectOutputStream out) {
    }

    private void readObject(ObjectInputStream in) {
    }
}
