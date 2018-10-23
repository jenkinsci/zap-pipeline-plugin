package com.vrondakis.zap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ZapFailBuildActionTest extends ZapTests {
    private ZapFailBuildAction failBuildAction;

    @Before
    public void setup() throws IOException, ExecutionException, InterruptedException {
        super.setup();
        failBuildAction = new ZapFailBuildAction();
    }

    @Test
    public void verifyActionData() {
        Assert.assertNull(failBuildAction.getIconFileName());
        Assert.assertNull(failBuildAction.getDisplayName());
        Assert.assertNull(failBuildAction.getUrlName());
    }

    @Test
    public void verifyActionAdded() {
        job.addAction(failBuildAction);
        Assert.assertNotNull(job.getAction(ZapFailBuildAction.class));
    }
}
