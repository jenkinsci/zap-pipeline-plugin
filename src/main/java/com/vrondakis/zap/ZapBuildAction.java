package com.vrondakis.zap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.Run;
import jenkins.tasks.SimpleBuildStep;

/**
 * Zap build action Sets up the sidebar Jenkins buttons
 */

public abstract class ZapBuildAction implements Action, SimpleBuildStep.LastBuildAction {
    private final Run<?, ?> run;

    ZapBuildAction(Run<?, ?> run) {
        this.run = run;
    }

    public Run<?, ?> getRun() {
        return this.run;
    }

    public String getIconFileName() {
        return "/plugin/zap-jenkins-plugin/logo.png";
    }

    public String getDisplayName() {
        return "ZAP Scanning Report";
    }

    public String getUrlName() {
        return Constants.DIRECTORY_NAME;
    }

    // Called by Jenkins
    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", ""); // Allow JS scripts to be run (content security policy)

        DirectoryBrowserSupport dbs = new DirectoryBrowserSupport(this, new FilePath(this.dir()), this.getTitle(),
                        "/plugin/zap-jenkins-plugin/logo.png", false);

        if (req.getRestOfPath().equals("")) {
            File file = new File(run.getRootDir(), "zap/index.html");
            long lastModified = file.lastModified();
            long length = file.length();

            try (InputStream in = new FileInputStream(file)) {
                rsp.serveFile(req, in, lastModified, length, file.getName());
            }

            return;
        }

        dbs.generateResponse(req, rsp, this);
    }

    protected File dir() {
        return null;
    }

    protected abstract String getTitle();

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Collections.singleton(this);
    }
}