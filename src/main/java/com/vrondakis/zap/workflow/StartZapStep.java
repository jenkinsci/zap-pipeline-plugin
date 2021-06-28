package com.vrondakis.zap.workflow;

import java.util.List;

import javax.annotation.CheckForNull;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;

public class StartZapStep extends Step {
    private final StartZapStepParameters zapStepParameters;

    /**
     * Called with startZap() in jenkinsfile
     *
     * @param host         The host to run ZAP on - default localhost
     * @param port         The port to run ZAP on - default 9092
     * @param timeout      The amount of seconds to let the ZAP attack run for before it quits - default 1000
     * @param zapHome      Where the zap process is located - this must be set if you are not using an external managed ZAP application.
     * @param allowedHosts The hosts to allow scans to begin on, if none are specified then it will run the attack locally only
     * @param sessionPath  Optional path to the session file
     * @param externalZap  Set to true, ZAP application is externally managed.
     * @param rootCaFile   the root CA file that can be exported using 'zap.sh -daemon -certfulldump cert.pem
     * @param additionalConfigurations  configurations to add to ZAP startup, in the form of key=value pairs. If empty, only default configurations are added
     */
    @DataBoundConstructor
    public StartZapStep(@CheckForNull String host, int port, int timeout, String zapHome, List<String> allowedHosts,
                        String sessionPath, boolean externalZap, String rootCaFile, List<String> additionalConfigurations) {
        zapStepParameters = new StartZapStepParameters(host, port, timeout, zapHome, allowedHosts, sessionPath, externalZap, rootCaFile, additionalConfigurations);

    }

    @Override
    public StepExecution start(StepContext context) {
        return new StartZapExecution(context, zapStepParameters);
    }

    @Extension
    public static class DescriptorImpl extends DefaultStepDescriptorImpl<StartZapExecution> {
        public DescriptorImpl() {
            super(StartZapExecution.class, "startZap", "Start ZAP process");
        }
    }
}