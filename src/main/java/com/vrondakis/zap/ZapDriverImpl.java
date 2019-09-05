package com.vrondakis.zap;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.vrondakis.zap.workflow.RunZapAttackStepParameters;

import hudson.FilePath;
import hudson.Launcher;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class ZapDriverImpl implements ZapDriver {
    private String zapHost;
    private int zapPort;
    private int zapTimeout;
    private HashMap<Integer, Integer> failBuild = new HashMap<>();
    private List<String> allowedHosts = new ArrayList<>();
    private final List<Integer> startedScans = new ArrayList<>();
    private int crawlId;


    /**
     * Calls the ZAP api
     *
     * @param apiUrl The API endpoint to call
     * @param params Map with GET Parameters for the call
     * @return JSONObject or null
     */
    private JSONObject zapApi(String apiUrl, Map<String, String> params) {
        try {
            String query = ZapDriverController.formatParams(params);

            apiUrl = "/JSON/" + apiUrl;
            URI uri = new URI("http", null, getZapHost(), getZapPort(), apiUrl, query, null);

            InputStream response = Unirest.get(uri.toString()).asString().getRawBody();

            String res = IOUtils.toString(response, StandardCharsets.UTF_8);
            return JSONObject.fromObject(res);

        } catch (URISyntaxException | IOException | UnirestException e) {
            // Should be handled in calling function
            e.printStackTrace();
            return null;
        }
    }

    private JSONObject zapApi(String apiUrl) {
        return zapApi(apiUrl, Collections.emptyMap());
    }

    public boolean shutdownZap() {
        if (0 == zapPort || null == zapHost) return false;

        return zapApi("core/action/shutdown") != null;
    }

    public boolean setZapMode(String mode) {
        Map<String, String> arguments = Collections.singletonMap("mode", mode);
        return zapApi("core/action/setMode", arguments) != null;
    }

    /**
     * Starts the ZAP crawler on a specified URL
     *
     * @param host The host to attack
     * @return Success
     */
    public boolean startZapCrawler(String host) {
        if (crawlId != 0) {
            return false;
        }

        try {
            // Start the scan on a particular site
            Map<String, String> arguments = Collections.singletonMap("url", host);
            JSONObject result = zapApi("spider/action/scan", arguments);

            if (result != null) {
                crawlId = result.getInt("scan");
                return true;
            }
        } catch (JSONException e) {
            // Return below
        }

        return false;
    }

    /**
     * Gets the current status of the started attacks (average of all)
     *
     * @return The % complete
     */
    public int zapCrawlerStatus() {
        Map<String, String> arguments = Collections.singletonMap("scanId", Integer.toString(crawlId));
        try {
            JSONObject json = zapApi("spider/view/status", arguments);
            if (json != null) {
                return json.getInt("status");
            } else {
                return COMPLETED_PERCENTAGE; // Failed to retrieve status so skip it
            }

        } catch (JSONException e) {
            return COMPLETED_PERCENTAGE;
        }
    }

    public boolean zapCrawlerSuccess() {
        OffsetDateTime startedTime = OffsetDateTime.now();
        int timeoutSeconds = this.getZapTimeout();

        int status = this.zapCrawlerStatus();
        while (status < ZapDriver.COMPLETED_PERCENTAGE) {
            if (OffsetDateTime.now().isAfter(startedTime.plusSeconds(timeoutSeconds))) {
                return false;
            }

            status = this.zapCrawlerStatus();
            System.out.println("zap: Crawler progress is: " + status + "%");

            try {
                // Stop spamming ZAP with requests as soon as one completes. Status won't have changed in a short time & don't pause
                // when the scan is complete.
                if (status != ZapDriver.COMPLETED_PERCENTAGE)
                    TimeUnit.SECONDS.sleep(ZapDriver.ZAP_SCAN_SLEEP);
            } catch (InterruptedException e) {
                // Usually if Jenkins run is stopped
                System.out.println("zap: Failed to get status of crawler");
            }
        }

        return true;
    }


    /**
     * Imports URLs from a text file
     *
     * @param path - The path to load from
     * @return Success
     */
    public boolean importUrls(String path) {
        System.out.println("zap: Importing URLs from " + path);
        Map<String, String> arguments = Collections.singletonMap("filePath", path);

        JSONObject result = zapApi("importurls/action/importurls", arguments);
        return result != null && result.has("Result") && result.getString("Result").equals("OK");
    }

    /**
     * Loads a ZAP session
     *
     * @param sessionPath - The path of the .session file
     * @return Success
     */
    public boolean loadSession(String sessionPath) {
        System.out.println("zap: Loading session from " + sessionPath);
        Map<String, String> arguments = Collections.singletonMap("name", sessionPath);
        JSONObject result = zapApi("core/action/loadSession", arguments);

        return result != null && result.has("Result") && result.getString("Result").equals("OK");
    }

    /**
     * Loads a ZAP policy from a file path
     *
     * @param policy - The path to load from
     * @return Success
     */
    public boolean loadPolicy(String policy) {
        Map<String, String> arguments = Collections.singletonMap("path", policy);
        JSONObject result = zapApi("ascan/action/importScanPolicy", arguments);

        if (result == null)
            return false;

        return (result.has("Result") && result.getString("Result").equals("OK"))
                || (result.has("code") && result.getString("code").equals("already_exists"));
    }

    /**
     * Starts the ZAP attack. If allowedHosts is not provided in jenkinsfile, it will scan only hosts that are local
     *
     * @param zsp The parameters from the groovy step
     * @return Success
     */
    public boolean zapAttack(RunZapAttackStepParameters zsp) {
        // Reset scans
        startedScans.clear();

        JSONObject sitesObj = zapApi("core/view/sites");
        if (sitesObj == null)
            return false;

        List<String> scanUrls = new ArrayList<>();
        JSONArray sites = sitesObj.getJSONArray("sites");
        for (Object site : sites) {
            String url = site.toString();

            // Only starts the scan if a scan on the site isn't currently running
            boolean found = scanUrls.stream().anyMatch(scan -> scan.equals(site.toString()));
            if (!found) {
                if (beginScan(url, zsp))
                    scanUrls.add(url);
            }

        }

        return true;
    }

    /**
     * Begins a scan on a selected URL if it is in the allowed hosts parameter or if it is local (and allowed hosts parameter is
     * empty)
     *
     * @param url The URL to scan. Does not include ZAP host prefix
     * @return Success
     */
    private boolean beginScan(String url, RunZapAttackStepParameters zsp) {
        try {
            List<String> allowedHosts = this.allowedHosts;
            String host = new URI(url).getHost();
            // If it is in the allowed hosts parameter - or if the url is unset if it is local
            // localhost.localdomain does not resolve properly with INetAddress.getByName, which is why there is an additional check
            if (!host.equals("localhost.localdomain")) {
                if (this.allowedHosts.isEmpty()) {
                    InetAddress addr = null;
                    try {
                        addr = InetAddress.getByName(host);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (addr == null)
                        return false;

                    if (!addr.isAnyLocalAddress() && !addr.isLoopbackAddress())
                        return false;
                } else if (!allowedHosts.contains(host)) {
                    System.out.println("zap: Host " + host + " is not in the allowedHosts parameter and is not a local host. Not scanning.");
                    return false;
                }
            }

            // Start the scan on a particular site with a particular user
            String attackUrl = "ascan/action/scan";
            Map<String, String> arguments = new HashMap<>();
            arguments.put("url", url);

            if (zsp.getUser() != 0) {
                System.out.println("zap: Loading user ID: " + zsp.getUser());
                attackUrl += "AsUser";
                arguments.put("userId", Integer.toString(zsp.getUser()));
            }

            if (zsp.getScanPolicyName() != null && !zsp.getScanPolicyName().isEmpty()) {
                arguments.put("scanPolicyName", zsp.getScanPolicyName());
            }

            JSONObject result = zapApi(attackUrl, arguments);
            if (result != null) {
                int zapScanId = result.getInt("scan");
                startedScans.add(zapScanId);
                return true;
            }

        } catch (JSONException | URISyntaxException e) {
            return false;
        }

        return false;
    }

    /**
     * Gets the current status of the started attacks (average of all)
     *
     * @return The % complete
     */
    public int zapAttackStatus() {
        int totalScans = startedScans.size();
        int totalProgress = 0;

        if (startedScans.isEmpty()) {
            // Called but no scans running
            return COMPLETED_PERCENTAGE;
        }

        for (Integer startedScan : startedScans) {
            int totalScanProgress = 0;

            Map<String, String> arguments = Collections.singletonMap("scanId", Integer.toString(startedScan));
            try {
                JSONObject json = zapApi("ascan/view/status", arguments);
                if (json != null) {
                    int status = json.getInt("status");
                    totalScanProgress += status;
                } else {
                    totalScanProgress = COMPLETED_PERCENTAGE; // Failed to retrieve status so skip it
                }
            } catch (JSONException e) {
                totalScanProgress = COMPLETED_PERCENTAGE;
            }

            totalProgress += totalScanProgress;
        }

        return totalProgress / (totalScans);
    }

    /**
     * Starts the ZAP process
     *
     * @param zapHome  - The location of the zap.sh file
     * @param ws       - Passed by step
     * @param launcher - Passed by step
     * @return Success
     */
    public boolean startZapProcess(String zapHome, FilePath ws, Launcher launcher) {
        List<String> cmd = new ArrayList<>();

        Path zapPath = Paths.get(zapHome,
                launcher.isUnix() ? ZapDriverController.ZAP_UNIX_PROGRAM : ZapDriverController.ZAP_WIN_PROGRAM);
        cmd.add(zapPath.toString());

        cmd.add(ZapDriverController.CMD_DAEMON);

        cmd.add(ZapDriverController.CMD_HOST);
        cmd.add(zapHost);

        cmd.add(ZapDriverController.CMD_PORT);
        cmd.add(Integer.toString(zapPort));

        cmd.add(ZapDriverController.CMD_CONFIG);
        cmd.add(ZapDriverController.CMD_DISABLEKEY);

        cmd.add(ZapDriverController.CMD_CONFIG);
        cmd.add(ZapDriverController.CMD_REGEX);

        cmd.add(ZapDriverController.CMD_CONFIG);
        cmd.add(ZapDriverController.CMD_NAME);

        cmd.add(ZapDriverController.CMD_CONFIG);
        cmd.add(ZapDriverController.CMD_TIMEOUT);

        try {
            launcher.launch().cmds(cmd).pwd(ws).start();
            launcher.getListener().getLogger().println("zap: Started successfully");
            return true;
        } catch (Exception e) {
            launcher.getListener().getLogger().println("zap: An error occurred while staring ZAP" + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean zapAliveCheck() {
        OffsetDateTime startedTime = OffsetDateTime.now();
        while (!OffsetDateTime.now().isAfter(startedTime.plusSeconds(ZapDriver.ZAP_INITIALIZE_TIMEOUT))) {
            try {
                TimeUnit.SECONDS.sleep(ZapDriver.ZAP_INITIALIZE_WAIT);
                System.out.println("zap: Attempting to connect to ZAP at " + this.getZapHost() + ":" + this.getZapPort());

                new Socket(this.getZapHost(), this.getZapPort());
                return true;
            } catch (IOException e) {
                // Do nothing, just means ZAP hasn't started yet - we want to wait for the timeout
            } catch (InterruptedException e) {
                break;
            }
        }

        return false;
    }

    public String getZapReport() throws IOException, UnirestException, URISyntaxException {
        URI uri = new URI("http", null, zapHost, zapPort, "/OTHER/core/other/jsonreport",
                "formMethod=GET", null);

        InputStream response = Unirest.get(uri.toString()).asString().getRawBody();
        return IOUtils.toString(response, StandardCharsets.UTF_8);
    }

    public String getZapReportXML() throws IOException, UnirestException, URISyntaxException {
        URI uri = new URI("http", null, zapHost, zapPort, "/other/core/other/xmlreport",
                "formMethod=GET", null);

        InputStream response = Unirest.get(uri.toString()).asString().getRawBody();
        return IOUtils.toString(response, StandardCharsets.UTF_8);
    }

    public void setZapHost(String zapHost) {
        this.zapHost = zapHost;
    }

    public void setZapPort(int zapPort) {
        this.zapPort = zapPort;
    }

    public void setFailBuild(int all, int high, int med, int low) {
        failBuild.put(ZapArchive.ALL_ALERT, all);
        failBuild.put(ZapArchive.HIGH_ALERT, high);
        failBuild.put(ZapArchive.MEDIUM_ALERT, med);
        failBuild.put(ZapArchive.LOW_ALERT, low);
    }

    public void setZapTimeout(int timeout) {
        zapTimeout = timeout;
    }

    public void setAllowedHosts(List<String> allowedHosts) {
        this.allowedHosts = allowedHosts;
    }

    public int getZapTimeout() {
        return zapTimeout;
    }

    public int getZapPort() {
        return zapPort;
    }

    public HashMap<Integer, Integer> getFailBuild() {
        return failBuild;
    }

    public String getZapHost() {
        return zapHost;
    }

    public List<String> getAllowedHosts() {
        return allowedHosts;
    }
}