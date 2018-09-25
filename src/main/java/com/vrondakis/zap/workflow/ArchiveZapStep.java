package com.vrondakis.zap.workflow;

import java.io.IOException;
import java.io.Serializable;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;

public class ArchiveZapStep extends Step implements Serializable {
    private final ArchiveZapStepParameters archiveZapStepParameters;

    /**
     * Analyse the zap attack and generate the zap report
     *
     * @param failAllAlerts Fail the build if there is x or more of any type of alert - default 0 (disabled)
     * @param failHighAlerts Fail the build when there is x or more of HIGH risk alerts - default 1
     * @param failMediumAlerts Fail the build when there is x or more of MEDIUM risk alerts - default 0 (disabled)
     * @param failLowAlerts Fail the build when there is more x or more LOW risk alerts - default 0 (disabled)
     */
    @DataBoundConstructor
    public ArchiveZapStep(Integer failAllAlerts, Integer failHighAlerts, Integer failMediumAlerts, Integer failLowAlerts) {
        this.archiveZapStepParameters = new ArchiveZapStepParameters(failAllAlerts, failHighAlerts, failMediumAlerts,
                        failLowAlerts);
    }

    @Override
    public StepExecution start(StepContext context) throws IOException, InterruptedException {
        return new ArchiveZapExecution(context, archiveZapStepParameters);
    }

    @Extension
    public static class DescriptorImpl extends DefaultStepDescriptorImpl<ArchiveZapExecution> {
        public DescriptorImpl() {
            super(ArchiveZapExecution.class, "archiveZap", "Create & Archive ZAP report");
        }
    }
}