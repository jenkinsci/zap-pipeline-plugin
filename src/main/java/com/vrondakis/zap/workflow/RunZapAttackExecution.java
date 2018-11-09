package com.vrondakis.zap.workflow;

import com.vrondakis.zap.ZapDriver;
import com.vrondakis.zap.ZapDriverController;
import hudson.model.Result;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Executor for zapAttack() function in jenkinsfile
 */
public class RunZapAttackExecution extends DefaultStepExecutionImpl {
    private RunZapAttackStepParameters runZapAttackStepParameters;

    public RunZapAttackExecution(StepContext context, RunZapAttackStepParameters runZapAttackStepParameters) {
        super(context);
        this.runZapAttackStepParameters = runZapAttackStepParameters;
    }

    @Override
    public boolean start() {
        listener.getLogger().println("zap: Starting attack...");
        ZapDriver zapDriver = ZapDriverController.getZapDriver(this.run);

        // Change to attack mode in ZAP
        boolean changeModeSuccess = zapDriver.setZapMode("attack");
        if (!changeModeSuccess) {
            listener.getLogger().println("zap: Failed to switch to attack mode");
            getContext().onSuccess(false);
            return false;
        }
        listener.getLogger().println("zap: Set mode to attack mode");

        // Start the attack on the collected urls
        boolean startAttackSuccess = zapDriver.zapAttack(runZapAttackStepParameters);
        if (!startAttackSuccess) {
            listener.getLogger().println("zap: Failed to start attack");
            getContext().onSuccess(false);
            return false;
        }

        OffsetDateTime startedTime = OffsetDateTime.now();

        int timeoutSeconds = zapDriver.getZapTimeout();
        int status = zapDriver.zapAttackStatus();

        while (status < ZapDriver.COMPLETED_PERCENTAGE) {
            if (OffsetDateTime.now().isAfter(startedTime.plusSeconds(timeoutSeconds))) {
                listener.getLogger().println("zap: Scan timed out before it could complete");
                getContext().setResult(Result.UNSTABLE);
                return true;
            }

            status = zapDriver.zapAttackStatus();
            listener.getLogger().println("zap: Scan progress is: " + status + "%");

            try {
                // Stop spamming ZAP with requests as soon as one completes. Status won't have changed in a short time & don't pause
                // when the scan is complete
                if (status != ZapDriver.COMPLETED_PERCENTAGE)
                    TimeUnit.SECONDS.sleep(ZapDriver.ZAP_SCAN_SLEEP);
            } catch (InterruptedException e) {
                // Usually if the Jenkins run is stopped
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