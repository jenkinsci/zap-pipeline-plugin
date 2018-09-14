package com.barracuda.zapcomp.workflow;

import com.barracuda.zapcomp.*;
import hudson.*;
import hudson.model.*;
import hudson.slaves.SlaveComputer;
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
        boolean isUnix = false;
        try {
            if ("Unix".equals(((SlaveComputer) node.toComputer()).getOSDescription())) isUnix = true;
        } catch (NullPointerException | InterruptedException | IOException e) {
            this.listener.getLogger().println("zap-comp: Could not start ZAP. Failed to retrieve OS information");
            getContext().onSuccess(false);
            return false;
        }

        if (node.getNodeName().isEmpty()) {
            launcher = new Launcher.LocalLauncher(listener, ws.getChannel());
        } else {
            launcher = new Launcher.RemoteLauncher(listener, ws.getChannel(), isUnix);
        }

        StartZapStepParameters zsp = step.getParameters();
        if (zsp == null) {
            this.listener.getLogger().println("zap-comp: Could not start ZAP. No parameters are provided - startZap");
            getContext().onFailure(new Throwable("zap-comp: Could not start ZAP. No parameters are provided - startZap"));
            return false;
        }



        this.listener.getLogger().println("zap-comp: Starting ZAP on port " + zsp.getPort() + "...");

        if (zsp.getZapHome() == null || zsp.getZapHome().isEmpty()) {
            System.out.println("zap-comp: Did not start ZAP process because zapHome is not set");
            getContext().onSuccess(true);
            return true;
        }

        ZapDriver zapDriver = ZapDriverController.newDriver(this.build);

        // Set ZAP properties
        zapDriver.setZapHost(zsp.getHost());
        zapDriver.setZapPort(zsp.getPort());
        zapDriver.setZapTimeout(zsp.getTimeout());
        zapDriver.setFailBuild(zsp.getFailBuild());
        zapDriver.setAllowedHosts(zsp.getAllowedHosts());

        boolean success = zapDriver.startZapProcess(zsp.getZapHome(), ws, launcher);

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

        // Load session
        if (zsp.getSessionPath() != null && !zsp.getSessionPath().isEmpty()) {
            System.out.println("zap-comp: Loading session " + zsp.getSessionPath());
            boolean loadedSession = zapDriver.loadSession(zsp.getSessionPath());
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
