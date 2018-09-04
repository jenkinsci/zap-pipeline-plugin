package com.barracuda.zapcomp.workflow;

import com.barracuda.zapcomp.*;
import hudson.*;
import hudson.model.*;
import org.jenkinsci.plugins.workflow.steps.*;

import java.io.*;
import java.net.*;
import java.time.*;
import java.util.concurrent.*;

/**
 * Executor for startZap() function in jenkinsfile
 */
public class RunZapCrawlerExecution extends AbstractStepExecutionImpl {

    private TaskListener listener;
    private FilePath ws;
    private Node node;
    private RunZapCrawlerStep step;

    public RunZapCrawlerExecution(StepContext context, RunZapCrawlerStep step) {
        super(context);
        System.out.println("in zapcrawler");

        try {
            this.step = step;
            this.node = context.get(Node.class);
            this.ws = context.get(FilePath.class);
            this.listener = context.get(TaskListener.class);

        } catch (IOException | InterruptedException e) {
            this.listener.getLogger().println("zap-comp: Failed to run ZAP crawler");
            getContext().onFailure(e);
        }
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

        boolean success = ZapDriver.startZapCrawler(zsp.getHost());

        if (!success) {
            System.out.println("zap-comp: Failed to start ZAP crawler on host " + zsp.getHost());
            getContext().onFailure(new Throwable("zap-comp: Failed ot start ZAP crawler on host " + zsp.getHost()));
            return false;
        }

        OffsetDateTime startedTime = OffsetDateTime.now();
        int timeoutSeconds = ZapDriver.getZapTimeout();
        int status = ZapDriver.zapCrawlerStatus();

        while (status < Constants.COMPLETED_PERCENTAGE) {
            if (OffsetDateTime.now().isAfter(startedTime.plusSeconds(timeoutSeconds))) {
                listener.getLogger().println("zap-comp: Crawler timed out before it could complete the scan");
                break;
            }

            status = ZapDriver.zapCrawlerStatus();
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

                new Socket(ZapDriver.getZapHost(), ZapDriver.getZapPort());
                listener.getLogger().println("zap-comp: ZAP successfully initialized on port " + ZapDriver.getZapPort());
                zapHasStarted = true;

            } catch (IOException e) {
                listener.getLogger().println("zap-comp: Waiting for ZAP to initialize...");
            } catch (InterruptedException e) {
                listener.getLogger().println(
                        "zap-comp: ZAP failed to initialize on host " + ZapDriver.getZapHost() + ":" + ZapDriver.getZapPort());
                break;
            }

        } while (!zapHasStarted);

        if (!zapHasStarted) {
            System.out.println("zap-comp: Failed to start ZAP on port " + ZapDriver.getZapPort());
            getContext().onFailure(
                    new Throwable("zap-comp: Failed to start ZAP on port " + ZapDriver.getZapPort() + ". Socket timed out"));

            return false;
        }

        getContext().onSuccess(true);
        return true;

    }

    @Override
    public void onResume() {
        super.onResume();
        start();
    }

    // findbugs fails without this because "non-transient non-serializable instance field in serializable class" because AbstractStepExecutionImpl has a base parent of Serializable
    private void writeObject(ObjectOutputStream out) {
    }

    private void readObject(ObjectInputStream in) {
    }
}
