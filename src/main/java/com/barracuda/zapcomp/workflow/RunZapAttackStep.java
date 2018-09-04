package com.barracuda.zapcomp.workflow;

import hudson.*;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.*;

import javax.annotation.CheckForNull;


public class RunZapAttackStep extends Step {
    private final RunZapAttackStepParameters zsp;

    /**
     * For zapAttack() function in Jenkinsfile
     *
     * @param scanPolicyName The scan policy name to use when attacking (optional). Make sure to load the policy from a file first using loadScanPolicy()
     * @param userId         The ZAP user ID to run the attack with, loaded from the context (optional)
     */

    @DataBoundConstructor
    public RunZapAttackStep(String scanPolicyName, int userId) {
        this.zsp = new RunZapAttackStepParameters(scanPolicyName, userId);
    }

    @CheckForNull
    public RunZapAttackStepParameters getParameters() {
        return zsp;
    }


    @Override
    public StepExecution start(StepContext context) {
        return new RunZapAttackExecution(context, this);
    }

    @Extension
    public static class DescriptorImpl extends DefaultStepDescriptorImpl<RunZapAttackExecution> {
        public DescriptorImpl() {
            super(RunZapAttackExecution.class, "runZapAttack", "Run ZAP attack by changing to attack mode and starting the attack");
        }
    }

}