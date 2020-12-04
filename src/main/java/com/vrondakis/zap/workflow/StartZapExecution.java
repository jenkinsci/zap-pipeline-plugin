package com.vrondakis.zap.workflow;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
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
            this.listener.getLogger().println("zap: Did not start ZAP process because zapHome is not set");
            getContext().onFailure(new Throwable("zap: Did not start ZAP process because zapHome is not set"));
            return false;
        }

        // Make sure workspace dir exists before launching Zap
        // See https://github.com/jenkinsci/zap-pipeline-plugin/issues/8
        try {
            if (!workspace.exists()) {
                launcher.getListener().getLogger().println("Creating workspace directory...");
                workspace.mkdirs();
            }
        } catch (Exception e) {
            launcher.getListener().getLogger().println("Unable to create workspace dir: " + e.toString());
            e.printStackTrace();
            return false;
        }

        // create a temp folder where zap will write
        File zapDir = null;
        try {
            zapDir = Files.createTempDirectory("zaptemp").toFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ZapDriver zapDriver = ZapDriverController.newDriver(this.run);
        // Set ZAP properties
        zapDriver.setZapHost(zapStepParameters.getHost());
        zapDriver.setZapDir(zapDir.getAbsolutePath());
        zapDriver.setZapPort(zapStepParameters.getPort());
        zapDriver.setZapTimeout(zapStepParameters.getTimeout());
        zapDriver.setAllowedHosts(zapStepParameters.getAllowedHosts());


        boolean success = zapDriver.startZapProcess(zapStepParameters.getZapHome(), workspace, launcher);
        if (!success) {
            this.listener.getLogger().println("zap: Failed to start ZAP process");
            getContext().onFailure(new Throwable("zap: Failed to start ZAP process"));
            return false;
        }

        this.listener.getLogger().println("zap: Checking ZAP is alive at " + zapStepParameters.getHost() + ":" + zapStepParameters.getPort());

        // Wait for ZAP to start before continuing...
        listener.getLogger().println("zap: Waiting for ZAP to initialize...");
        boolean zapHasStarted = zapDriver.zapAliveCheck();

        if (!zapHasStarted) {
            this.listener.getLogger().println("zap: Failed to start ZAP on port " + zapDriver.getZapPort());
            getContext().onFailure(
                    new Throwable("zap: Failed to start ZAP on " + zapDriver.getZapHost() + ":" + zapDriver.getZapPort() + ". Socket timed out"));

            return false;
        }

        // Load session
        if (zapStepParameters.getSessionPath() != null && !zapStepParameters.getSessionPath().isEmpty()) {
            this.listener.getLogger().println("zap: Loading session " + zapStepParameters.getSessionPath());
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
