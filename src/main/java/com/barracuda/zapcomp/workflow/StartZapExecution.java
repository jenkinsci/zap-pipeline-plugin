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
public class StartZapExecution extends DefaultStepExecutionImpl {
    private StartZapStep step;

    public StartZapExecution(StepContext context, StartZapStep step) {
        super(context);
        this.step = step;
    }

    @Override
    public boolean start() {
        if (node.getNodeName().isEmpty()) {
            launcher = new Launcher.LocalLauncher(listener, ws.getChannel());
        } else {
            launcher = new Launcher.RemoteLauncher(listener, ws.getChannel(), true);
        }

        StartZapStepParameters zsp = step.getParameters();
        if (zsp == null) {
            this.listener.getLogger().println("zap-comp: Could not start ZAP. No parameters are provided - startZap");
            getContext().onFailure(new Throwable("zap-comp: Could not start ZAP. No parameters are provided - startZap"));
            return false;
        }

        // Set ZAP properties
        ZapDriver.setZapHost(zsp.getHost());
        ZapDriver.setZapPort(zsp.getPort());
        ZapDriver.setZapTimeout(zsp.getTimeout());
        ZapDriver.setFailBuild(zsp.getFailBuild());
        ZapDriver.setAllowedHosts(zsp.getAllowedHosts());

        this.listener.getLogger().println("zap-comp: Starting ZAP on port " + zsp.getPort() + "...");

        if (zsp.getZapHome() == null || zsp.getZapHome().isEmpty()) {
            System.out.println("zap-comp: Did not start ZAP process because zapHome is not set");
            getContext().onSuccess(true);
            return true;
        }

        boolean success = ZapDriver.startZapProcess(zsp.getZapHome(), ws, launcher);

        if (!success) {
            System.out.println("zap-comp: Failed to start ZAP process");
            getContext().onFailure(new Throwable("zap-comp: Failed to start ZAP process"));
            return false;
        }

        OffsetDateTime startedTime = OffsetDateTime.now();
        listener.getLogger().println("zap-comp: Waiting for ZAP to initialize...");

        boolean zapHasStarted = false;

        // Wait for ZAP to start before continuing...
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

        // Load session
        if (zsp.getSessionPath() != null && !zsp.getSessionPath().isEmpty()) {
            System.out.println("zap-comp: Loading session " + zsp.getSessionPath());
            boolean loadedSession = ZapDriver.loadSession(zsp.getSessionPath());
            if (!loadedSession) getContext().onFailure(new Throwable("zap-comp: Could not load session file"));
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
