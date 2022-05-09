package com.vrondakis.zap.workflow;

import com.vrondakis.zap.PluginProgress;
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
import java.util.List;
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
    public Void run() throws Exception {
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

        int loops = 0;
        while (status < ZapDriver.COMPLETED_PERCENTAGE) {
            if (OffsetDateTime.now().isAfter(startedTime.plusSeconds(timeoutSeconds))) {
                listener.getLogger().println("zap: Scan timed out before it could complete");
                getContext().setResult(Result.UNSTABLE);
                getContext().onSuccess(true);
                return null;
            }

            status = zapDriver.zapAttackStatus();

            if (loops % (ZapDriver.ZAP_SCAN_STATUS_PRINT_INTERVAL / ZapDriver.ZAP_SCAN_SLEEP) == 0 || status == ZapDriver.COMPLETED_PERCENTAGE) {
                listener.getLogger().println("\nzap: Scan progress is: " + status + "%");

                List<PluginProgress> progress = zapDriver.zapAttackProgress();
                listener.getLogger().println("|------------------------------------------|----------------------|------------|------------|");
                listener.getLogger().println(String.format("| %-40s | %-20s | %10s | %10s |", "Plugin", "Progress", "Requests", "Alerts"));
                listener.getLogger().println("|------------------------------------------|----------------------|------------|------------|");
                for (PluginProgress plugin: progress) {
                    listener.getLogger().println(String.format("| %-40.40s | %-20.20s | %10.10s | %10.10s |", plugin.getName(), plugin.getStatus(), plugin.getRequests(), plugin.getAlerts()));
                }
                listener.getLogger().println("|------------------------------------------|----------------------|--------------|--------------|");
            } else {
                listener.getLogger().print(".");
            }
            loops++;

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