package com.vrondakis.zap.workflow;

import com.vrondakis.zap.ZapDriver;
import com.vrondakis.zap.ZapDriverController;
import com.vrondakis.zap.ZapExecutionException;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Executor for zapAttack() function in jenkinsfile
 */
public class RunZapAttackExecution extends SynchronousNonBlockingStepExecution<Void> {
    private RunZapAttackStepParameters runZapAttackStepParameters;

    public RunZapAttackExecution(StepContext context, RunZapAttackStepParameters runZapAttackStepParameters) {
        super(context);
        this.runZapAttackStepParameters = runZapAttackStepParameters;
    }

    @Override
    protected Void run() throws Exception {
        TaskListener listener = getContext().get(TaskListener.class);;
        Run<?, ?> run = getContext().get(Run.class);
        listener.getLogger().println("zap: Starting attack...");
        ZapDriver zapDriver = ZapDriverController.getZapDriver(run, listener.getLogger());

        // Change to attack mode in ZAP
        try {
            zapDriver.setZapMode("attack");
            listener.getLogger().println("zap: Set mode to attack mode");
        } catch (Exception e) {
            getContext().onSuccess(new ZapExecutionException("Failed to switch ZAP to attack mode.", e, listener.getLogger()));
            return null;
        }

        // Start the attack on the collected urls
        try {
            zapDriver.zapAttack(runZapAttackStepParameters);
        } catch (Exception e) {
            getContext().onSuccess(new ZapExecutionException("Failed to start attack.", e, listener.getLogger()));
            return null;
        }

        OffsetDateTime startedTime = OffsetDateTime.now();

        int timeoutSeconds = zapDriver.getZapTimeout();
        int status = zapDriver.zapAttackStatus();

        int oldStatus = -1;
        while (status < ZapDriver.COMPLETED_PERCENTAGE) {
            if (OffsetDateTime.now().isAfter(startedTime.plusSeconds(timeoutSeconds))) {
                listener.getLogger().println("zap: Scan timed out before it could complete");
                getContext().setResult(Result.UNSTABLE);
                getContext().onSuccess(true);
                return null;
            }

            status = zapDriver.zapAttackStatus();
            if (oldStatus!=status) {
                listener.getLogger().print("\nzap: Scan progress is: " + status + "%");
                oldStatus = status;
            }
            else {
                listener.getLogger().print(".");
            }

            try {
                // Stop spamming ZAP with requests as soon as one completes. Status won't have changed in a short time & don't pause
                // when the scan is complete
                if (status != ZapDriver.COMPLETED_PERCENTAGE) {
                    TimeUnit.SECONDS.sleep(ZapDriver.ZAP_SCAN_SLEEP);
                }
            } catch (InterruptedException e) {
                getContext().onSuccess(new ZapExecutionException("\nFailed to complete attack.", e, listener.getLogger()));
                return null;
            }
        }

        listener.getLogger().print("\n");
        getContext().onSuccess(true);
        return null;
    }

    // findbugs fails without this because "non-transient non-serializable instance field in serializable class"
    private void writeObject(ObjectOutputStream out) {
    }

    private void readObject(ObjectInputStream in) {
    }
}