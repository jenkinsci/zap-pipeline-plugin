package com.barracuda.zapcomp;

import com.google.gson.*;
import com.google.gson.reflect.*;
import com.mashape.unirest.http.*;
import com.mashape.unirest.http.exceptions.*;
import hudson.*;
import hudson.model.*;
import hudson.tasks.*;
import jenkins.model.*;
import net.sf.json.*;
import org.apache.commons.io.*;

import javax.annotation.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.*;
import java.util.stream.*;

/**
 * ZapCompare Main zap-comp class, handles report files &amp; compares reports
 */

public class ZapCompare extends Recorder {
    private static final String RAW_REPORT_FILENAME = "zap-raw.json";
    private static final String RAW_OLD_REPORT_FILENAME = "zap-raw-old.json";

    /**
     * Saves index.html to the current build archive
     *
     * @param run The current build
     * @return If it saved successfully or not
     */
    private boolean saveStaticFiles(@Nonnull Run<?, ?> run) {
        try {
            String indexName = "index.html";
            String pluginName = "zap-comp";
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
    private boolean saveZapReport(File path, Run build) {
        System.out.println("Running saveZapReport()...");
        try {
            ZapDriver zapDriver = ZapDriverController.getZapDriver(build);
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
     * Retrieves the previous report and copies it to the new build workspace
     *
     * @param path    The path to save the old report
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
     * Helper function to open report file & parse the JSON
     *
     * @param path - The path of the report
     * @return The JSONObject of the report or null
     */
    private JSONObject openReportFile(FilePath path) {
        try {
            String report = path.readToString();

            return JSONObject.fromObject(report);
        } catch (IOException | InterruptedException | JSONException e) {
            return null;
        }
    }

    /**
     * Gets the current builds saved ZAP report - used when comparing the report to the previous report
     *
     * @param path     - The path of the current report
     * @param fileName - The filename of the report, eg zap-report.json
     * @return The JSONObject of the report
     */
    private JSONObject getSavedZapReport(File path, String fileName) {
        FilePath fp = new FilePath(new File(path.toString() + "/" + fileName));
        return openReportFile(fp);
    }

    /**
     * Gets the report of the last build, if it is not available try the previous built build, and if that isn't available get the
     * last successful build.
     *
     * @param run The build
     * @return The file path of the last available report, null if none are found
     */
    private Optional<File> getPreviousReportDir(Run<?, ?> run) {

        File zapBuildDir = getBuildDir(run.getPreviousBuild())
                .orElseGet(() -> getBuildDir(run.getPreviousBuiltBuild())
                        .orElseGet(() -> getBuildDir(run.getPreviousSuccessfulBuild()).orElse(null)));


        return Optional.ofNullable(zapBuildDir);
    }

    private Optional<File> getBuildDir(Run<?, ?> build) {
        if (build != null) {
            File buildDir = new File(build.getRootDir(), Constants.DIRECTORY_NAME);
            if (buildDir.exists())
                return Optional.of(buildDir);
        }

        return Optional.empty();
    }

    /**
     * Archives the current raw ZAP JSON report &amp; saves static files
     *
     * @param run          - The current build
     * @param taskListener - Logging
     * @return If it was a success or not
     */
    public boolean archiveRawReport(@Nonnull Run<?, ?> run, @Nonnull TaskListener taskListener) {
        File zapDir = new File(run.getRootDir(), Constants.DIRECTORY_NAME);

        if (!zapDir.exists()) {
            boolean mResult = zapDir.mkdir();
            if (!mResult) {
                taskListener.getLogger().println(
                        "zap-comp: Could not create directory at " + zapDir.toURI().toString() + " (archiveRawReport)");
                return false;
            }
        }

        // Adds the sidebar menu UI button
        ZapAction action = new ZapAction(run);
        run.addAction(action);

        // Fetches the JSON report from ZAP and saves it
        if (!saveZapReport(zapDir, run))
            return false;

        // Saves the report of the previous build in the current builds workspace
        Optional<File> oldBuildZapDir = getPreviousReportDir(run);
        oldBuildZapDir.ifPresent(file -> savePreviousZapReport(zapDir, file));

        // Saves index.html file
        return saveStaticFiles(run);
    }

    @SuppressWarnings("unchecked")
    private List<ZapAlert> zapReportToAlertList(Object report) {
        JSONArray sites;

        // ZAP returns [{site}, {site}] if there are multiple sites, and just {site} if there is one (not an array with one
        // element);
        if (report instanceof JSONArray) {
            sites = (JSONArray) report;
        } else {
            sites = new JSONArray();
            sites.add(report);
        }
        System.out.println("sites are this:");
        System.out.println(sites.toString());

        // Converts all the alerts in the site array into ZapAlerts and adds them to the alerts
        Function<JSONObject, Stream<? extends ZapAlert>> alerts = site -> ((ArrayList<ZapAlert>) new Gson()
                .fromJson(site.getJSONArray("alerts").toString(), new TypeToken<List<ZapAlert>>() {
                }.getType())).stream();

        return sites.stream().map(site -> (JSONObject) site).flatMap(alerts).collect(Collectors.toList());
    }

    /**
     * Checks 2 reports and checks if the newest has any new critical alerts, only run if failBuild is set to true in the parameters
     * of archiveZap
     *
     * @param run      - The current build
     * @param listener - Logging
     * @return If it has new critical alerts or not
     */
    public boolean hasNewCriticalAlerts(Run<?, ?> run, TaskListener listener) {
        listener.getLogger().println("zap-comp: Checking results...");

        File zapDir = new File(run.getRootDir(), Constants.DIRECTORY_NAME);

        ZapDriver zapDriver = ZapDriverController.getZapDriver(run);

        //JSONObject previousBuildReport = getSavedZapReport(zapDir, RAW_OLD_REPORT_FILENAME);
        JSONObject currentBuildReport = getSavedZapReport(zapDir, RAW_REPORT_FILENAME);

        // Get alerts in the new report
        String jsonSiteName = "site";
        try {
            Object sites = currentBuildReport.get(jsonSiteName);
            if (sites == null)
                return false; // No sites in report? did ZAP run correctly?

            List<ZapAlert> currentBuildAlerts = zapReportToAlertList(sites);
            Map<Integer, Integer> alertCounts = new HashMap<>();

            // Count the number of alerts, {high:5, medium:1, low:2}
            currentBuildAlerts.forEach(alert -> {
                int riskCode = Integer.parseInt(alert.getRiskcode());
                alertCounts.put(riskCode, alertCounts.containsKey(riskCode) ? alertCounts.get(riskCode) + 1 : 1);
            });

            // Total amount of alerts with a risk code more than 1 (.count returns a long)
            alertCounts.put(Constants.ALL_ALERT,
                    currentBuildAlerts.stream()
                            .filter(alert -> Integer.parseInt(alert.getRiskcode()) > 0)
                            .map(e -> 1)
                            .reduce(0, Integer::sum)
            );

            AtomicBoolean hasNewCriticalAlerts = new AtomicBoolean(false);
            // Compare the fail build parameter to the amount of alerts in a certain category
            zapDriver.getFailBuild().forEach((code, val) -> {
                if (val > 0 && alertCounts.get(code) >= val) {
                    hasNewCriticalAlerts.set(true);
                }
            });


            return hasNewCriticalAlerts.get();

            /*
            // If the previous build report could not be found, check the current report for any alerts with critical alerts.
            if (previousBuildReport == null)
                return currentBuildAlerts.stream()
                        .anyMatch(newAlert -> Integer.parseInt(newAlert.getRiskcode()) >= zapDriver.getFailBuild());

            Object previousBuildSites = previousBuildReport.get(jsonSiteName);
            List<ZapAlert> previousBuildAlerts = new ArrayList<>();
            if (previousBuildSites != null) {
                previousBuildAlerts = zapReportToAlertList(previousBuildSites);
            }

            // Check all the new alerts for their risk codes and fail accordingly
            final List<ZapAlert> finalPreviousBuildAlerts = previousBuildAlerts;

            return currentBuildAlerts.stream()
                    .filter(newAlert -> finalPreviousBuildAlerts.stream().noneMatch(oldAlert -> oldAlert.equals(newAlert)))
                    .anyMatch(alert -> Integer.parseInt(alert.getRiskcode()) >= zapDriver.getFailBuild());
            */

        } catch (NullPointerException e) {
            listener.getLogger().println("zap-comp: Could not determine weather build has new alerts.");
            return false;
        }
    }

    // For Jenkins
    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return null;
    }
}