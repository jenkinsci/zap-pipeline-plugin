package com.vrondakis.zap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ZapActionTest extends ZapTests {

    private ZapTrendChart chart;

    @Before
    public void setup() throws IOException, ExecutionException, InterruptedException {
        super.setup();
        chart = new ZapTrendChart(job);
    }

    @Test
    public void verifyActionData() {
        // If displayName, iconFileName are not null then there will be an additional button on the sidebar
        Assert.assertNull(chart.getDisplayName());
        Assert.assertNull(chart.getIconFileName());
        Assert.assertEquals(ZapArchive.DIRECTORY_NAME, chart.getUrlName());
    }

    @Test
    public void verifyActionAdded() {
        job.addAction(chart);
        Assert.assertNotNull(job.getAction(ZapTrendChart.class));
    }
}