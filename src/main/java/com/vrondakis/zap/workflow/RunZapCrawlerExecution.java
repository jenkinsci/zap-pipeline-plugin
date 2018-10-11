package com.vrondakis.zap.workflow;

import com.vrondakis.zap.Constants;
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
        if (runZapCrawlerParameters == null) {
            this.listener.getLogger().println("zap: Could not run ZAP crawler, no host has been provided");
            getContext().onSuccess(false);
            return false;
        }

        this.listener.getLogger().println("zap: Starting crawler on host " + runZapCrawlerParameters.getHost() + "...");
        ZapDriver zapDriver = ZapDriverController.getZapDriver(this.run);

        boolean success = zapDriver.startZapCrawler(runZapCrawlerParameters.getHost());
        if (!success) {
            System.out.println("zap: Failed to start ZAP crawler on host " + runZapCrawlerParameters.getHost());
            getContext().onFailure(new Throwable("zap: Failed ot start ZAP crawler on host " + runZapCrawlerParameters.getHost()));
            return false;
        }

        OffsetDateTime startedTime = OffsetDateTime.now();
        int timeoutSeconds = zapDriver.getZapTimeout();

        int status = zapDriver.zapCrawlerStatus();
        while (status < Constants.COMPLETED_PERCENTAGE) {
            if (OffsetDateTime.now().isAfter(startedTime.plusSeconds(timeoutSeconds))) {
                listener.getLogger().println("zap: Crawler timed out before it could complete the scan");
                break;
            }

            status = zapDriver.zapCrawlerStatus();
            listener.getLogger().println("zap: Crawler progress is: " + status + "%");

            try {
                // Stop spamming ZAP with requests as soon as one completes. Status won't have changed in a short time & don't pause
                // when the scan is complete.
                if (status != Constants.COMPLETED_PERCENTAGE)
                    TimeUnit.SECONDS.sleep(Constants.SCAN_SLEEP);
            } catch (InterruptedException e) {
                // Usually if Jenkins run is stopped
                System.out.println("zap: Failed to get status of crawler");
            }
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
