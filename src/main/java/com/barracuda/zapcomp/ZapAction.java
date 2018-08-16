package com.barracuda.zapcomp;

import hudson.model.Run;

import java.io.File;

/**
 * ZapAction
 * Used by jenkins to add the sidebar button
 */
public class ZapAction extends ZapBuildAction {
    private final Run<?, ?> build;

    public ZapAction(Run<?, ?> build) {
        super(build);
        this.build = build;
    }

    @Override
    protected String getTitle() {
        return this.build.getDisplayName();
    }

    @Override
    protected File dir() {
        return new File(build.getRootDir(), Constants.DIRECTORY_NAME);
    }
}