package com.vrondakis.zap.workflow;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import org.jenkinsci.plugins.workflow.steps.StepContext;

import com.vrondakis.zap.Constants;
import com.vrondakis.zap.ZapDriver;
import com.vrondakis.zap.ZapDriverController;

import hudson.Launcher;

/**
 * Executor for startZap() function in jenkinsfile
 */
public class StartZapExecution extends DefaultStepExecutionImpl {
    private StartZapStepParameters zapStepParameters;

    public StartZapExecution(StepContext context, StartZapStepParameters zapStepParameters) {
        super(context);
        this.zapStepParameters = zapStepParameters;
    }

    @Override
    public boolean start() {
        if (node.getNodeName().isEmpty()) {
            launcher = new Launcher.LocalLauncher(listener, ws.getChannel());
        } else {
            try {
                launcher = new Launcher.RemoteLauncher(listener, ws.getChannel(), node.toComputer().isUnix());
            } catch (NullPointerException e) {
                this.listener.getLogger().println("zap: Could not start ZAP. Failed to retrieve OS information");
                getContext().onSuccess(false);
                return false;
            }
        }

        if (zapStepParameters == null) {
            this.listener.getLogger().println("zap: Could not start ZAP. No parameters are provided - startZap");
            getContext().onFailure(new Throwable("zap: Could not start ZAP. No parameters are provided - startZap"));
            return false;
        }

        this.listener.getLogger().println("zap: Starting ZAP on port " + zapStepParameters.getPort() + "...");
        if (zapStepParameters.getZapHome() == null || zapStepParameters.getZapHome().isEmpty()) {
            System.out.println("zap: Did not start ZAP process because zapHome is not set");
            getContext().onSuccess(true);
            return true;
        }

        ZapDriver zapDriver = ZapDriverController.newDriver(this.build);

        // Set ZAP properties
        zapDriver.setZapHost(zapStepParameters.getHost());
        zapDriver.setZapPort(zapStepParameters.getPort());
        zapDriver.setZapTimeout(zapStepParameters.getTimeout());
        zapDriver.setAllowedHosts(zapStepParameters.getAllowedHosts());

        boolean success = zapDriver.startZapProcess(zapStepParameters.getZapHome(), ws, launcher);
        if (!success) {
            System.out.println("zap: Failed to start ZAP process");
            getContext().onFailure(new Throwable("zap: Failed to start ZAP process"));
            return false;
        }

        // Wait for ZAP to start before continuing...
        OffsetDateTime startedTime = OffsetDateTime.now();
        listener.getLogger().println("zap: Waiting for ZAP to initialize...");
        boolean zapHasStarted = false;
        do {
            if (OffsetDateTime.now().isAfter(startedTime.plusSeconds(Constants.ZAP_INITIALIZE_TIMEOUT))) {
                listener.getLogger().println("zap: ZAP failed to start. Socket timed out.");
                break;
            }

            try {
                TimeUnit.SECONDS.sleep(Constants.ZAP_INITIALIZE_WAIT);

                new Socket(zapDriver.getZapHost(), zapDriver.getZapPort());
                listener.getLogger().println("zap: ZAP successfully initialized on port " + zapDriver.getZapPort());
                zapHasStarted = true;

            } catch (IOException e) {
                listener.getLogger().println("zap: Waiting for ZAP to initialize...");
            } catch (InterruptedException e) {
                listener.getLogger().println(
                    "zap: ZAP failed to initialize on host " + zapDriver.getZapHost() + ":" + zapDriver.getZapPort());
                break;
            }

        } while (!zapHasStarted);

        if (!zapHasStarted) {
            System.out.println("zap: Failed to start ZAP on port " + zapDriver.getZapPort());
            getContext().onFailure(
                new Throwable("zap: Failed to start ZAP on port " + zapDriver.getZapPort() + ". Socket timed out"));

            return false;
        }

        // Load session
        if (zapStepParameters.getSessionPath() != null && !zapStepParameters.getSessionPath().isEmpty()) {
            System.out.println("zap: Loading session " + zapStepParameters.getSessionPath());
            boolean loadedSession = zapDriver.loadSession(zapStepParameters.getSessionPath());
            if (!loadedSession)
                getContext().onFailure(new Throwable("zap: Could not load session file"));
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
