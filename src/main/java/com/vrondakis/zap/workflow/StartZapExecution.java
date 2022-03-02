package com.vrondakis.zap.workflow;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import com.vrondakis.zap.ZapExecutionException;
import org.apache.commons.lang.ObjectUtils;
import hudson.FilePath;
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
    public Boolean run() {
        Launcher launcher;
        // Linux vs Windows & master vs slave
        if (node.getNodeName().isEmpty()) {
            launcher = new Launcher.LocalLauncher(listener, workspace.getChannel());
        } else {
            try {
                launcher = new Launcher.RemoteLauncher(listener, workspace.getChannel(), node.toComputer().isUnix());
            } catch (NullPointerException e) {
                getContext().onFailure(new ZapExecutionException("Could not start ZAP. Failed to retrieve OS information", e, listener.getLogger()));
                return false;
            }
        }

        // No parameters
        if (zapStepParameters == null) {
            getContext().onFailure(new ZapExecutionException("Could not start ZAP. No parameters are provided.", listener.getLogger()));
            return false;
        }

        // Zap home not set / invalid
        this.listener.getLogger().println("zap: Starting ZAP on port " + zapStepParameters.getPort() + "...");
        if ((zapStepParameters.getZapHome() == null || zapStepParameters.getZapHome().isEmpty()) && !zapStepParameters.isExternalZap()) {
            getContext().onFailure(new ZapExecutionException("Did not start ZAP process because zapHome is not set, and external ZAP is not true.", listener.getLogger()));
            return false;
        }

        // Make sure workspace dir exists before launching Zap
        // See https://github.com/jenkinsci/zap-pipeline-plugin/issues/8
        try {
            if (!workspace.exists()) {
                listener.getLogger().println("Creating workspace directory...");
                workspace.mkdirs();
            }
        } catch (Exception e) {
            getContext().onFailure(new ZapExecutionException("Unable to create workspace dir.", e, listener.getLogger()));
            return false;
        }

        // create a temp folder where zap will write
        FilePath zapDir = null;
        try {
            zapDir = workspace.createTempDir( "zaptemp", "");
        } catch (IOException | InterruptedException e) {
            getContext().onFailure(new ZapExecutionException("Unable to create temporary zap folder.", e, listener.getLogger()));
            return false;
        }

        ZapDriver zapDriver = ZapDriverController.newDriver(this.run, listener.getLogger());
        // Set ZAP properties
        zapDriver.setZapHost(zapStepParameters.getHost());
        zapDriver.setZapDir(zapDir);
        zapDriver.setZapPort(zapStepParameters.getPort());
        zapDriver.setZapTimeout(zapStepParameters.getTimeout());
        zapDriver.setAllowedHosts(zapStepParameters.getAllowedHosts());
        zapDriver.setZapRootCaFile(zapStepParameters.getRootCaFile());
        zapDriver.setAdditionalConfigurations(zapStepParameters.getAdditionalConfigurations());

        if (zapStepParameters.isExternalZap()) {
            listener.getLogger().println("zap: Run marked as external zap, skipping startup of the zap instance.");
        } else {
            try {
                zapDriver.startZapProcess(zapStepParameters.getZapHome(), workspace, launcher);
            } catch (Exception e) {
                getContext().onFailure(new ZapExecutionException("Failed to start ZAP process.", e, listener.getLogger()));
                return false;
            }
        }

        this.listener.getLogger().println("zap: Checking ZAP is alive at " + zapStepParameters.getHost() + ":" + zapStepParameters.getPort());

        // Wait for ZAP to start before continuing...
        listener.getLogger().println("zap: Waiting for ZAP to initialize...");

        try {
            zapDriver.zapAliveCheck();
        } catch (Exception e) {
            getContext().onFailure(new ZapExecutionException("Failed to start ZAP on " + zapDriver.getZapHost() + ":" + zapDriver.getZapPort(), e, listener.getLogger()));
            return false;
        }

        // Load session
        if (zapStepParameters.getSessionPath() != null && !zapStepParameters.getSessionPath().isEmpty()) {
            this.listener.getLogger().println("zap: Loading session " + zapStepParameters.getSessionPath());

            try {
                zapDriver.loadSession(zapStepParameters.getSessionPath());
            } catch (Exception e) {
                getContext().onFailure(new ZapExecutionException("Could not load session file.", e, listener.getLogger()));
                return false;
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
