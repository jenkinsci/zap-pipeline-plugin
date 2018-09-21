package com.barracuda.zapcomp.workflow;

import hudson.*;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.*;

import javax.annotation.*;
import java.util.*;

public class StartZapStep extends Step {
    private final StartZapStepParameters zsp;

    /**
     * Called with startZap() in jenkinsfile
     *
     * @param host             The host to run ZAP on - default localhost
     * @param port             The port to run ZAP on - default 9092
     * @param timeout          The amount of seconds to let the ZAP attack run for before it quits - default 1000
     * @param failAllAlerts    Fail the build if there is a new critical alert - default 0
     * @param failHighAlerts   Fail the build when there is more than x amount of HIGH risk alerts
     * @param failMediumAlerts Fail the build when there is more than x amount of MEDIUM risk alerts
     * @param failLowAlerts    Fail the build when there is more than x amount of LOW risk alerts
     * @param zapHome          Where the zap process is located - if not set this will not start ZAP (but still make calls if you're running it locally)
     * @param allowedHosts     The hosts to allow scans to begin on, if none are specified then it will run the attack locally only
     * @param sessionPath      Optional path to the session file
     */

    @DataBoundConstructor
    public StartZapStep(@CheckForNull String host, int port, int timeout,
                        int failAllAlerts, int failHighAlerts, int failMediumAlerts, int failLowAlerts, String zapHome, List<String> allowedHosts, String sessionPath) {
        if (host == null)
            host = "localhost";

        if (port == 0)
            port = 9092;

        if (timeout == 0)
            timeout = 1000;

        if (zapHome == null || zapHome.isEmpty())
            zapHome = System.getProperty("ZAP_HOME");

        if (allowedHosts == null || allowedHosts.isEmpty()) {
            allowedHosts = new ArrayList<>();
        }

        this.zsp = new StartZapStepParameters(host, port, timeout, failAllAlerts, failHighAlerts, failMediumAlerts, failLowAlerts, zapHome, allowedHosts, sessionPath);
    }

    @CheckForNull
    public StartZapStepParameters getParameters() {
        return zsp;
    }

    @Override
    public StepExecution start(StepContext context) {
        return new StartZapExecution(context, this);
    }

    @Extension
    public static class DescriptorImpl extends DefaultStepDescriptorImpl<StartZapExecution> {
        public DescriptorImpl() {
            super(StartZapExecution.class, "startZap", "Start ZAP process");
        }
    }
}