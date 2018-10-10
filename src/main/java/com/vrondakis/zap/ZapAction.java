package com.vrondakis.zap;

import java.io.File;

import hudson.model.Run;

/**
 * ZapAction Used by jenkins to add the sidebar button
 */

public class ZapAction extends ZapBuildAction {
    private final Run<?, ?> run;

    public ZapAction(Run<?, ?> run) {
        super(run);
        this.run = run;
    }

    @Override
    protected String getTitle() {
        return this.run.getDisplayName();
    }

    @Override
    protected File dir() {
        return new File(run.getRootDir(), Constants.DIRECTORY_NAME);
    }
}