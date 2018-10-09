package com.vrondakis.zap;

import com.google.gson.Gson;
import hudson.FilePath;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class ZapTrendChart implements Action {
    public final Job<?, ?> job;

    public ZapTrendChart(Job<?, ?> job) {
        this.job = job;
    }

    // Gets the amount of alerts for every build in a map
    public Map<Integer, ZapAlertCount> getAlertCounts(Run<?, ?> thisRun) {
        Map<Integer, ZapAlertCount> counts = new HashMap<>();

        job.getBuildsAsMap().forEach((k, v) -> {
            ZapAlertCount count = getZapAlertCountForBuild(new File(v.getRootDir(), Constants.DIRECTORY_NAME));
            if (null != count)
                counts.put(k, count);
        });

        return counts;
    }

    // Gets the amount of alerts for a specific build
    public ZapAlertCount getZapAlertCountForBuild(File zapDir) {
        FilePath filePath = new FilePath(new File(zapDir.toString() + "/" + "alert-count.json"));

        try {
            String jsonRaw = filePath.readToString();
            Gson gson = new Gson();
            return gson.fromJson(jsonRaw, ZapAlertCount.class);
        } catch (InterruptedException | IOException e) {
            return null;
        }
    }

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return Constants.DIRECTORY_NAME;
    }

}
