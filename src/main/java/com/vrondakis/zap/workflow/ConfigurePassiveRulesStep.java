package com.vrondakis.zap.workflow;

import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;

public class ConfigurePassiveRulesStep extends Step {
    private final ConfigurePassiveRulesStepParameters configurePassiveRulesStepParameters;

    @DataBoundConstructor
    public ConfigurePassiveRulesStep(@CheckForNull String action, @CheckForNull Integer ... ids) {
        this.configurePassiveRulesStepParameters = new ConfigurePassiveRulesStepParameters(action, ids);
    }

    @Override
    public StepExecution start(StepContext context) {
        return new ConfigurePassiveRulesExecution(context, configurePassiveRulesStepParameters);
    }

    @Extension
    public static class DescriptorImpl extends DefaultStepDescriptorImpl<ConfigurePassiveRulesExecution> {
        public DescriptorImpl() {
            super(ConfigurePassiveRulesExecution.class, "configurePassiveRules", "Configures the list of passive rules to apply / avoid (https://www.zaproxy.org/docs/alerts/)");
        }
    }
}