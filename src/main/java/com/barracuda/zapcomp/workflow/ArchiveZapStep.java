package com.barracuda.zapcomp.workflow;

import hudson.*;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.*;

import java.io.*;

@Extension
public class ArchiveZapStep extends Step implements Serializable {
    @DataBoundConstructor
    public ArchiveZapStep() {
    }

    @Override
    public StepExecution start(StepContext context) {
        return new ArchiveZapExecution(context);
    }

    @Extension
    public static class DescriptorImpl extends DefaultStepDescriptorImpl<ArchiveZapExecution> {
        public DescriptorImpl() {
            super(ArchiveZapExecution.class, "archiveZap", "Create & Archive ZAP report");
        }
    }


}
