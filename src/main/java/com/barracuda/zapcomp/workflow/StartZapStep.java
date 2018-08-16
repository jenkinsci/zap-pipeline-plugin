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
     * @param host The host to run ZAP on - default localhost
     * @param port The port to run ZAP on - default 9092
     * @param timeout The amount of seconds to let the ZAP attack run for before it quits - default 1000
     * @param failBuild Fail the build if there is a new critical alert - default 0
     * @param zapHome Where the zap process is located - default /opt/zap/
     * @param allowedHosts The hosts to allow scans to begin on, if none are specified then it will run the attack locally only
     */


    @DataBoundConstructor
    public StartZapStep(@CheckForNull String host, int port, int timeout,
                        int failBuild, @CheckForNull String zapHome, List<String> allowedHosts) {

        if (host == null)
            host = "localhost";

        if (port == 0)
            port = 9092;

        if (timeout == 0)
            timeout = 1000;

        if (zapHome == null || zapHome.equals(""))
            zapHome = System.getProperty("ZAP_HOME");

        if (zapHome == null || zapHome.equals(""))
            zapHome = "/opt/zap";

        if (allowedHosts == null || allowedHosts.isEmpty()){
            allowedHosts = new ArrayList<>();
        }

        this.zsp = new StartZapStepParameters(host, port, timeout, failBuild, zapHome, allowedHosts);
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