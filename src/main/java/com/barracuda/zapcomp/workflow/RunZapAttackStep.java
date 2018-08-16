package com.barracuda.zapcomp.workflow;

import hudson.*;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.*;


public class RunZapAttackStep extends Step {

    /**
     * For zapAttack() function in Jenkinsfile
     */

    @DataBoundConstructor
    public RunZapAttackStep() {
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