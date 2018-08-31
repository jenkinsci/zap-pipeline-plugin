package com.barracuda.zapcomp.workflow;

import com.barracuda.zapcomp.*;
import hudson.model.*;
import org.jenkinsci.plugins.workflow.steps.*;

import java.io.*;
import java.time.*;
import java.util.concurrent.*;

/**
 * Executor for zapAttack() function in jenkinsfile
 */
public class RunZapAttackExecution extends AbstractStepExecutionImpl {
    private TaskListener listener;
    private RunZapAttackStep step;

    public RunZapAttackExecution(StepContext context, RunZapAttackStep step) {
        super(context);

        try {
            this.step = step;
            this.listener = context.get(TaskListener.class);
        } catch (IOException | InterruptedException e) {
            getContext().onFailure(e);
        }
    }

    @Override
    public boolean start() {
        listener.getLogger().println("zap-comp: Starting attack...");

        boolean changeModeSuccess = ZapDriver.setZapMode("attack");
        if (!changeModeSuccess) {
            listener.getLogger().println("zap-comp: Failed to switch to attack mode");
            getContext().onSuccess(false);
            return false;
        }

        listener.getLogger().println("zap-comp: Set mode to attack mode");

        RunZapAttackStepParameters zsp = step.getParameters();
        boolean startAttackSuccess = ZapDriver.zapAttack(zsp);
        if (!startAttackSuccess) {
            listener.getLogger().println("zap-comp: Failed to start attack");
            getContext().onSuccess(false);
            return false;
        }

        OffsetDateTime startedTime = OffsetDateTime.now();

        int timeoutSeconds = ZapDriver.getZapTimeout();
        int status = ZapDriver.zapAttackStatus();

        while (status < Constants.COMPLETED_PERCENTAGE) {
            if (OffsetDateTime.now().isAfter(startedTime.plusSeconds(timeoutSeconds))) {
                listener.getLogger().println("zap-comp: Scan timed out before it could complete");
                break;
            }

            status = ZapDriver.zapAttackStatus();
            listener.getLogger().println("zap-comp: Scan progress is: " + status + "%");

            try {
                // Stop spamming ZAP with requests as soon as one completes. Status won't have changed in a short time & don't pause when the scan is complete
                if(status!=Constants.COMPLETED_PERCENTAGE)
                    TimeUnit.SECONDS.sleep(Constants.SCAN_SLEEP);
            } catch (InterruptedException e) {
                // Usually if the Jenkins build is stopped
            }
        }


        getContext().onSuccess(true);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        start();
    }

    // findbugs fails without this because "non-transient non-serializable instance field in serializable class"
    private void writeObject(ObjectOutputStream out) {
    }

    private void readObject(ObjectInputStream in) {
    }

}
