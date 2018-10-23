package com.vrondakis.zap.workflow;

import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.junit.Test;

public class ImportZapPolicyStepTest extends ZapWorkflow {
    @Test
    public void failNoParameters() throws Exception {
        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     importZapScanPolicy()\n"
                + "}"
        ));

        run = job.scheduleBuild2(0).get();
        r.assertBuildStatus(Result.FAILURE, run);
    }

    @Test
    public void parameterProvidedPass() throws Exception {
        job.setDefinition(new CpsFlowDefinition(""
                + "node('slave') {\n"
                + "     importZapScanPolicy(policyPath: '/var/policy.policy')\n"
                + "}"
        ));

        run = job.scheduleBuild2(0).get();
        System.out.println("parameter provided pass");

        r.assertBuildStatus(Result.SUCCESS, run);
    }
}