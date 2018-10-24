package com.vrondakis.zap.workflow;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import org.jenkinsci.plugins.workflow.steps.StepContext;
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
        // Linux vs Windows & master vs slave
        if (node.getNodeName().isEmpty()) {
            launcher = new Launcher.LocalLauncher(listener, workspace.getChannel());
        } else {
            try {
                launcher = new Launcher.RemoteLauncher(listener, workspace.getChannel(), node.toComputer().isUnix());
            } catch (NullPointerException e) {
                this.listener.getLogger().println("zap: Could not start ZAP. Failed to retrieve OS information");
                getContext().onSuccess(false);
                return false;
            }
        }

        // No parameters
        if (zapStepParameters == null) {
            this.listener.getLogger().println("zap: Could not start ZAP. No parameters are provided - startZap");
            getContext().onFailure(new Throwable("zap: Could not start ZAP. No parameters are provided - startZap"));
            return false;
        }

        // Zap home not set / invalid
        this.listener.getLogger().println("zap: Starting ZAP on port " + zapStepParameters.getPort() + "...");
        if (zapStepParameters.getZapHome() == null || zapStepParameters.getZapHome().isEmpty()) {
            System.out.println("zap: Did not start ZAP process because zapHome is not set");
            getContext().onFailure(new Throwable("zap: Did not start ZAP process because zapHome is not set"));
            return false;
        }

        ZapDriver zapDriver = ZapDriverController.newDriver(this.run);
        // Set ZAP properties
        zapDriver.setZapHost(zapStepParameters.getHost());
        zapDriver.setZapPort(zapStepParameters.getPort());
        zapDriver.setZapTimeout(zapStepParameters.getTimeout());
        zapDriver.setAllowedHosts(zapStepParameters.getAllowedHosts());


        boolean success = zapDriver.startZapProcess(zapStepParameters.getZapHome(), workspace, launcher);
        if (!success) {
            System.out.println("zap: Failed to start ZAP process");
            getContext().onFailure(new Throwable("zap: Failed to start ZAP process"));
            return false;
        }

        System.out.println("zap: Checking ZAP is alive at " + zapStepParameters.getHost() + ":" + zapStepParameters.getPort());

        // Wait for ZAP to start before continuing...
        listener.getLogger().println("zap: Waiting for ZAP to initialize...");
        boolean zapHasStarted = zapDriver.zapAliveCheck();

        if (!zapHasStarted) {
            System.out.println("zap: Failed to start ZAP on port " + zapDriver.getZapPort());
            getContext().onFailure(
                    new Throwable("zap: Failed to start ZAP on " + zapDriver.getZapHost() + ":" + zapDriver.getZapPort() + ". Socket timed out"));

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
