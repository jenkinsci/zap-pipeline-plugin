package com.vrondakis.zap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * ZapArchiver Main zap class, handles generating report.
 */

public class ZapArchiver extends Recorder {
    private static final String RAW_REPORT_FILENAME = "zap-raw.json";
    private static final String RAW_OLD_REPORT_FILENAME = "zap-raw-old.json";
    private static final String FALSE_POSITIVES_FILENAME = "zap-false-positives.json";
    private static final String JSON_SITE_KEY = "site";
    private static final String JSON_ALERTS_KEY = "alerts";

    /**
     * Saves index.html to the current build archive
     *
     * @param run The current build
     * @return If it saved successfully or not
     */
    private boolean saveStaticFiles(@Nonnull Run<?, ?> run) {
        try {
            String indexName = "index.html";
            String pluginName = "zap-jenkins-plugin";
            FilePath indexFile = new FilePath(new File(
                            Jenkins.getInstance().getPlugin(pluginName).getWrapper().baseResourceURL.getFile(), indexName));
            indexFile.copyTo(new FilePath(new File(run.getRootDir(), Constants.DIRECTORY_NAME + "/" + indexName)));
            return true;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    /**
     * Retrieves the ZAP report from ZAP and saves it in path
     *
     * @param path - Where to save the file
     * @return If it saved successfully or not
     */
    private boolean saveZapReport(File path, Run run) {
        System.out.println("Running saveZapReport()...");
        try {
            ZapDriver zapDriver = ZapDriverController.getZapDriver(run);
            FilePath fp = new FilePath(new File(path.toString() + "/" + RAW_REPORT_FILENAME));
            if (zapDriver.getZapHost() == null || zapDriver.getZapPort() == 0)
                return false;

            System.out.println("Got the correct file path");

            URI uri = new URI("http", null, zapDriver.getZapHost(), zapDriver.getZapPort(), "/OTHER/core/other/jsonreport",
                            "formMethod=GET", null);

            InputStream response = Unirest.get(uri.toString()).asString().getRawBody();
            System.out.println("the url is+ " + uri.toString());
            String res = IOUtils.toString(response, StandardCharsets.UTF_8);
            System.out.println("the response is " + res);
            fp.write(res, "UTF-8");

            return true;
        } catch (URISyntaxException | IOException | UnirestException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the previous report and copies it to the new build
     *
     * @param path The path to save the old report
     * @param oldPath The path of the old report
     */
    private void savePreviousZapReport(File path, File oldPath) {
        FilePath saveLocation = new FilePath(new File(path.toString() + "/" + RAW_OLD_REPORT_FILENAME));
        FilePath oldReport = new FilePath(new File(oldPath.toString() + "/" + RAW_REPORT_FILENAME));

        try {
            if (oldReport.exists()) {
                oldReport.copyTo(saveLocation);
            }
        } catch (Exception ignored) {
            // Probably it's the first build ran while using the plugin
        }
    }

    /**
     * Retrieves the false positives file (if available) from the workspace and saves it to the build
     *
     * @param falsePositivesFilePath the relative path to the false positives file
     * @param workspace the workspace for the running build
     * @param taskListener
     */
    private void saveFalsePositives(String falsePositivesFilePath, FilePath workspace, @Nonnull TaskListener taskListener,
                                    File savePath) {
        try {
            if (workspace != null) {
                System.out.println("Zap searching for false positives file in workspace: " + workspace.getName());
                FilePath[] falsePositivesFiles = workspace.list(falsePositivesFilePath);
                if (falsePositivesFiles.length > 0) {
                    if (falsePositivesFiles.length > 1) {
                        taskListener.getLogger()
                                        .println("zap: More than one file matched the provided false positives file path. Using: '"
                                                        + falsePositivesFiles[0].getName() + "'.");
                    }
                    System.out.println("Zap found false positives file:" + falsePositivesFiles[0].getName());
                    System.out.println(
                        "Zap saving false positives file to:" + savePath.toString() + "/" + FALSE_POSITIVES_FILENAME);
                    FilePath saveAsFile = new FilePath(new File(savePath.toString() + "/" + FALSE_POSITIVES_FILENAME));
                    falsePositivesFiles[0].copyTo(saveAsFile);
                }
                System.out.println("Zap finished saving false positives file");
            } else {
                taskListener.getLogger().println(
                    "zap: Failed to access workspace for false positives file, it may be on a non-connected slave. False positives will not be suppressed.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            taskListener.getLogger()
                            .println("zap: Failed to do archive false positives file. False positives will not be suppressed");
        }
    }

    /**
     * Helper function to open report file & parse the JSON
     *
     * @param path - The path of the report
     * @return The list of Zap Alerts from the JSON
     */
    private List<ZapAlert> getAlertsFromReportFile(FilePath path) {
        try {
            JSONObject report = JSONObject.fromObject(path.readToString());

            // Zap returns either an array of sites, or a single site as an object. Attempt to load as an array, then
            // fall back to object on fail
            JSONArray sites;
            try {
                sites = report.getJSONArray(JSON_SITE_KEY);
            } catch (JSONException e) {
                sites = new JSONArray();
                sites.add(report.getJSONObject(JSON_SITE_KEY));
            }

            // Iterate over all sites, and flatten alerts down to a single array
            List<ZapAlert> alerts = new ArrayList<>();
            for (Object site : sites) {
                String alertArrayString = JSONObject.fromObject(site).getJSONArray(JSON_ALERTS_KEY).toString();
                List<ZapAlert> siteAlerts = new Gson().fromJson(alertArrayString, new TypeToken<List<ZapAlert>>() {
                }.getType());
                alerts.addAll(siteAlerts);
            }
            return alerts;
        } catch (IOException | InterruptedException | JSONException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Gets all the ZapAlerts saved in a ZAP report file
     *
     * @param path - The path of the report
     * @param fileName - The filename of the report, eg zap-report.json
     * @return List of all the {@code ZapAlert}s from the report
     */
    private List<ZapAlert> getSavedZapReport(File path, String fileName) {
        FilePath fp = new FilePath(new File(path.toString() + "/" + fileName));
        return getAlertsFromReportFile(fp);
    }

    /**
     * Gets the report of the last build, if it is not available try the previous built build, and if that isn't available get the
     * last successful build.
     *
     * @param run The run
     * @return The file path of the last available report, null if none are found
     */
    private Optional<File> getPreviousReportDir(Run<?, ?> run) {

        File zapBuildDir = getBuildDir(run.getPreviousBuild()).orElseGet(() -> getBuildDir(run.getPreviousBuiltBuild())
                        .orElseGet(() -> getBuildDir(run.getPreviousSuccessfulBuild()).orElse(null)));

        return Optional.ofNullable(zapBuildDir);
    }

    /**
     * Gets the current builds false positives from a saved file
     *
     * @param path - The path of the saved false positives file
     * @param fileName - The filename of the false positives file
     * @return List of each false positive detailed in the file
     */
    private List<ZapFalsePositiveInstance> getSavedFalsePositives(File path, String fileName) {
        FilePath filePath = new FilePath(new File(path.toString() + "/" + fileName));
        try {
            String fileContents = filePath.readToString();
            return new Gson().fromJson(fileContents, new TypeToken<List<ZapFalsePositiveInstance>>() {
            }.getType());
        } catch (IOException | InterruptedException | JSONException e) {
            return Collections.emptyList();
        }
    }

    private Optional<File> getBuildDir(Run<?, ?> run) {
        if (run != null) {
            File buildDir = new File(run.getRootDir(), Constants.DIRECTORY_NAME);
            if (buildDir.exists())
                return Optional.of(buildDir);
        }

        return Optional.empty();
    }

    /**
     * Archives the current raw ZAP JSON report &amp; saves static files
     *
     * @param run - The current run
     * @param taskListener - Logging
     * @return If it was a success or not
     */
    public boolean archiveRawReport(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull TaskListener taskListener,
                                    String falsePositivesFilePath) {
        File zapDir = new File(run.getRootDir(), Constants.DIRECTORY_NAME);

        if (!zapDir.exists()) {
            boolean mResult = zapDir.mkdir();
            if (!mResult) {
                taskListener.getLogger()
                                .println("zap: Could not create directory at " + zapDir.toURI().toString() + " (archiveRawReport)");
                return false;
            }
        }

        // Fetches the JSON report from ZAP and saves it
        if (!saveZapReport(zapDir, run))
            return false;

        // Saves index.html file
        if (!saveStaticFiles(run))
            return false;

        // Adds the sidebar menu UI button
        ZapAction action = new ZapAction(run);
        run.addAction(action);

        // Fetches the false positives file (if it exists) and saves it
        saveFalsePositives(falsePositivesFilePath, workspace, taskListener, zapDir);

        // Saves the report of the previous build in the current builds workspace
        Optional<File> oldBuildZapDir = getPreviousReportDir(run);
        oldBuildZapDir.ifPresent(file -> savePreviousZapReport(zapDir, file));

        return true;
    }

    /**
     * Review the report, and fail the build according to given fail build parameters
     *
     * @param run - The current run
     * @param listener - Logging
     * @return If the build should be failed
     */
    public boolean shouldFailBuild(Run<?, ?> run, TaskListener listener) {
        listener.getLogger().println("zap: Checking results...");
        ZapDriver zapDriver = ZapDriverController.getZapDriver(run);

        try {
            // Collect the alerts and false positives associated with this build
            System.out.println("Zap collecting current build and false positives");
            File zapDir = new File(run.getRootDir(), Constants.DIRECTORY_NAME);
            List<ZapAlert> currentBuildAlerts = getSavedZapReport(zapDir, RAW_REPORT_FILENAME);
            List<ZapFalsePositiveInstance> zapFalsePositiveInstances = getSavedFalsePositives(zapDir, FALSE_POSITIVES_FILENAME);
            Map<Integer, Integer> alertCounts = new HashMap<>();

            // Count the number of alert instances (filtering out false positives)
            System.out.println("Zap filtering alerts for false positives");
            currentBuildAlerts.forEach(alert -> {
                int riskCode = Integer.parseInt(alert.getRiskcode());
                int filteredInstancesCount = alert.getFalsePositivesFilteredInstances(zapFalsePositiveInstances).size();
                int newCount = alertCounts.containsKey(riskCode) ? alertCounts.get(riskCode) + filteredInstancesCount
                                : filteredInstancesCount;
                alertCounts.put(riskCode, newCount);
            });

            // Total amount of alert instances with a risk code more than 1
            System.out.println("Zap counting total alerts");
            alertCounts.put(Constants.ALL_ALERT,
                (int) currentBuildAlerts.stream().filter(alert -> Integer.parseInt(alert.getRiskcode()) > 0).count());

            // Compare the fail build parameter to the amount of alerts in a certain category
            System.out.println("Zap counting alerts by risk level");
            AtomicBoolean failBuild = new AtomicBoolean(false);
            zapDriver.getFailBuild().forEach((code, val) -> {
                if (val > 0 && alertCounts.get(code) >= val) {
                    failBuild.set(true);
                }
            });
            System.out.println("Zap build fail status: " + failBuild.get());
            return failBuild.get();

        } catch (NullPointerException e) {
            listener.getLogger().println("zap: Could not determine whether the build has new alerts.");
            return false;
        }
    }

    // For Jenkins
    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return null;
    }
}