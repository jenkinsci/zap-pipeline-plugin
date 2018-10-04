package com.vrondakis.zap.workflow;

import java.io.IOException;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;

public class RunZapAttackStep extends Step {
    private final RunZapAttackStepParameters runZapAttackStepParameters;

    /**
     * For zapAttack() function in Jenkinsfile
     *
     * @param scanPolicyName The scan policy name to use when attacking (optional). Make sure to load the policy from a file first
     *        using loadScanPolicy()
     * @param userId The ZAP user ID to run the attack with, loaded from the context (optional)
     */
    @DataBoundConstructor
    public RunZapAttackStep(String scanPolicyName, int userId) {
        this.runZapAttackStepParameters = new RunZapAttackStepParameters(scanPolicyName, userId);
    }

    @Override
    public StepExecution start(StepContext context) throws IOException, InterruptedException {
        return new RunZapAttackExecution(context, runZapAttackStepParameters);
    }

    @Extension
    public static class DescriptorImpl extends DefaultStepDescriptorImpl<RunZapAttackExecution> {
        public DescriptorImpl() {
            super(RunZapAttackExecution.class, "runZapAttack", "Run ZAP attack by changing to attack mode and starting the attack");
        }
    }
}