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
     * Analyse the zap attack and generate the zap report, then stop the zap instance.
     *
     * @param failAllAlerts          Fail the run if there is x or more of any type of alert - default 0 (disabled)
     * @param failHighAlerts         Fail the run when there is x or more of HIGH risk alerts - default 1
     * @param failMediumAlerts       Fail the run when there is x or more of MEDIUM risk alerts - default 0 (disabled)
     * @param failLowAlerts          Fail the run when there is more x or more LOW risk alerts - default 0 (disabled)
     * @param falsePositivesFilePath File name and path (relative to workspace) to the falsePositives config file - default
     *                               "zapfalsePositives.json"
     * @param keepAlive              If true, the zap application will not be sent the shutdown command.
     * @param extendedReport         If true, the generated json report will contain more detailed information on each alert instance.
     */
    @DataBoundConstructor
    public ArchiveZapStep(Integer failAllAlerts, Integer failHighAlerts, Integer failMediumAlerts, Integer failLowAlerts,
                          String falsePositivesFilePath, boolean keepAlive, boolean extendedReport) {
        this.archiveZapStepParameters = new ArchiveZapStepParameters(failAllAlerts, failHighAlerts, failMediumAlerts,
                failLowAlerts, falsePositivesFilePath, keepAlive, extendedReport);
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