package com.vrondakis.zap;

import hudson.model.Run;
import jenkins.model.RunAction2;

import javax.annotation.CheckForNull;
import java.io.Serializable;


public class ZapFailBuildAction implements Serializable, RunAction2 {
    @CheckForNull
    private transient Run<?, ?> run;

    private static final long serialVersionUID = 1L;

    public ZapFailBuildAction(){

    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName(){
        return null;
    }

    @Override
    public String getUrlName(){
        return null;
    }

    @Override
    public void onAttached(Run<?, ?> run) {
        this.run = run;
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.run = run;
    }
}
