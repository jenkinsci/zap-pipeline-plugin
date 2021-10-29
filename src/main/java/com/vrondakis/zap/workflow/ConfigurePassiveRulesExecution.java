package com.vrondakis.zap.workflow;

import com.vrondakis.zap.ZapDriver;
import com.vrondakis.zap.ZapDriverController;
import com.vrondakis.zap.ZapExecutionException;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ConfigurePassiveRulesExecution extends DefaultStepExecutionImpl {
    private ConfigurePassiveRulesStepParameters configurePassiveRulesStepParameters;

    public ConfigurePassiveRulesExecution(StepContext context, ConfigurePassiveRulesStepParameters configurePassiveRulesStepParameters) {
        super(context);
        this.configurePassiveRulesStepParameters = configurePassiveRulesStepParameters;
    }

    @Override
    public boolean start() {
        listener.getLogger().println("zap: Configuring passive rules ...");

        if (configurePassiveRulesStepParameters == null
                || configurePassiveRulesStepParameters.getAction().isEmpty()
                || (!"enablePassiveScanners".equalsIgnoreCase(configurePassiveRulesStepParameters.getAction())
                    && !"disablePassiveScanners".equalsIgnoreCase(configurePassiveRulesStepParameters.getAction()))
        ) {
            getContext().onFailure(new ZapExecutionException("Action name should be 'enablePassiveScanners' or 'disablePassiveScanners'", listener.getLogger()));
            return false;
        }

        ZapDriver zapDriver = ZapDriverController.getZapDriver(this.run);

        try {
            if ("enablePassiveScanners".equalsIgnoreCase(configurePassiveRulesStepParameters.getAction())) {
                zapDriver.enablePassiveScanners(configurePassiveRulesStepParameters.getIds());
            } else if ("disablePassiveScanners".equalsIgnoreCase(configurePassiveRulesStepParameters.getAction())) {
                zapDriver.disablePassiveScanners(configurePassiveRulesStepParameters.getIds());
            }
        } catch (Exception e) {
            getContext().onSuccess(new ZapExecutionException("Failed to configure passive scanner rules " + configurePassiveRulesStepParameters.getIds(), e, listener.getLogger()));
            return false;
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
