package com.barracuda.zapcomp.workflow;

import com.barracuda.zapcomp.*;
import hudson.*;
import hudson.model.*;
import org.jaxen.expr.DefaultStep;
import org.jenkinsci.plugins.workflow.steps.*;

import java.io.*;
import java.net.*;
import java.time.*;
import java.util.concurrent.*;

/**
 * Executor for startZap() function in jenkinsfile
 */
public class RunZapCrawlerExecution extends DefaultStepExecutionImpl {
    private RunZapCrawlerStep step;

    public RunZapCrawlerExecution(StepContext context, RunZapCrawlerStep step) {
        super(context);
        this.step = step;
    }

    @Override
    public boolean start() {
        RunZapCrawlerParameters zsp = step.getParameters();
        if (zsp == null) {
            this.listener.getLogger().println("zap-comp: Could not run ZAP crawler, no host has been provided");
            getContext().onSuccess(false);
            return false;
        }

        this.listener.getLogger().println("zap-comp: Starting crawler on host " + zsp.getHost() + "...");

        ZapDriver zapDriver = ZapDriverController.getZapDriver(this.build);
        boolean success = zapDriver.startZapCrawler(zsp.getHost());
        if (!success) {
            System.out.println("zap-comp: Failed to start ZAP crawler on host " + zsp.getHost());
            getContext().onFailure(new Throwable("zap-comp: Failed ot start ZAP crawler on host " + zsp.getHost()));
            return false;
        }

        OffsetDateTime startedTime = OffsetDateTime.now();
        int timeoutSeconds = zapDriver.getZapTimeout();

        int status = zapDriver.zapCrawlerStatus();
        while (status < Constants.COMPLETED_PERCENTAGE) {
            if (OffsetDateTime.now().isAfter(startedTime.plusSeconds(timeoutSeconds))) {
                listener.getLogger().println("zap-comp: Crawler timed out before it could complete the scan");
                break;
            }

            status = zapDriver.zapCrawlerStatus();
            listener.getLogger().println("zap-comp: Crawler progress is: " + status + "%");

            try {
                // Stop spamming ZAP with requests as soon as one completes. Status won't have changed in a short time & don't pause when the scan is complete.
                if (status != Constants.COMPLETED_PERCENTAGE)
                    TimeUnit.SECONDS.sleep(Constants.SCAN_SLEEP);
            } catch (InterruptedException e) {
                // Usually if Jenkins build is stopped
            }
        }

        boolean zapHasStarted = false;

        do {
            if (OffsetDateTime.now().isAfter(startedTime.plusSeconds(Constants.ZAP_INITIALIZE_TIMEOUT))) {
                listener.getLogger().println("zap-comp: ZAP failed to start. Socket timed out.");
                break;
            }

            try {
                TimeUnit.SECONDS.sleep(Constants.ZAP_INITIALIZE_WAIT);

                new Socket(zapDriver.getZapHost(), zapDriver.getZapPort());
                listener.getLogger().println("zap-comp: ZAP successfully initialized on port " + zapDriver.getZapPort());
                zapHasStarted = true;

            } catch (IOException e) {
                listener.getLogger().println("zap-comp: Waiting for ZAP to initialize...");
            } catch (InterruptedException e) {
                listener.getLogger().println(
                        "zap-comp: ZAP failed to initialize on host " + zapDriver.getZapHost() + ":" + zapDriver.getZapPort());
                break;
            }

        } while (!zapHasStarted);

        if (!zapHasStarted) {
            System.out.println("zap-comp: Failed to start ZAP on port " + zapDriver.getZapPort());
            getContext().onFailure(
                    new Throwable("zap-comp: Failed to start ZAP on port " + zapDriver.getZapPort() + ". Socket timed out"));

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
