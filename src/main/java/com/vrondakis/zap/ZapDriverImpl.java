package com.vrondakis.zap;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.vrondakis.zap.workflow.RunZapAttackStepParameters;

import hudson.FilePath;
import hudson.Launcher;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ZapDriverImpl implements ZapDriver {
    private String zapHost;
    private int zapPort;
    private FilePath zapDir;
    private int zapTimeout;
    private HashMap<Integer, Integer> failBuild = new HashMap<>();
    private List<String> allowedHosts = new ArrayList<>();
    private final List<Integer> startedScans = new ArrayList<>();
    private int crawlId;
    private String rootCaFile;
    private List<String> additionalConfigurations = new ArrayList<>();
    private PrintStream logger = System.out;

    /**
     * Sets the current logger.
     * @param logger
     */
    public void setLogger(PrintStream logger) {
        this.logger = logger;
    }

    /**
     * Logs message using the currently set logger.
     * @param message the message to log
     */
    private void log(String message) {
        if (logger != null) {
            logger.println(message);
        }
    }

    /**
     * Calls the ZAP api
     *
     * @param apiUrl The API endpoint to call
     * @param params Map with GET Parameters for the call
     * @return JSONObject or null
     */
    private JSONObject zapApi(String apiUrl, Map<String, String> params) throws ZapExecutionException {
        try {
            String query = ZapDriverController.formatParams(params);

            apiUrl = "/JSON/" + apiUrl;
            URI uri = new URI("http", null, getZapHost(), getZapPort(), apiUrl, query, null);

            InputStream response = Unirest.get(uri.toString()).asString().getRawBody();

            String res = IOUtils.toString(response, StandardCharsets.UTF_8);
            JSONObject value = JSONObject.fromObject(res);
            if (value == null) {
                throw new ZapExecutionException("ZAP API returned an empty response.");
            }
            return value;
        } catch (ZapExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new ZapExecutionException("Failed call ZAP API.", e);
        }
    }

    private void verifyApiResultIsOk(JSONObject result, String errorMessage) throws ZapExecutionException {
        if (!result.has("Result") || !result.getString("Result").equals("OK")) {
            throw new ZapExecutionException(errorMessage);
        }
    }

    private JSONObject zapApi(String apiUrl) throws ZapExecutionException {
        return zapApi(apiUrl, Collections.emptyMap());
    }

    public void shutdownZap() throws ZapExecutionException {
        if (zapPort == 0 || zapHost == null) {
            throw new ZapExecutionException("Cannot shutdown Zap, missing Port and/or Host value.");
        }

        zapApi("core/action/shutdown");
    }

    public void setZapMode(String mode) throws ZapExecutionException {
        Map<String, String> arguments = Collections.singletonMap("mode", mode);
        zapApi("core/action/setMode", arguments);
    }

    /**
     * Starts the ZAP crawler on a specified URL
     *
     * @param host The host to attack
     */
    public void startZapCrawler(String host) throws ZapExecutionException {
        if (crawlId != 0) {
            throw new ZapExecutionException("ZAP Crawler already running");
        }

        // Start the scan on a particular site
        Map<String, String> arguments = Collections.singletonMap("url", host);
        JSONObject result = zapApi("spider/action/scan", arguments);

        crawlId = result.getInt("scan");
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
            return json.getInt("status");

        } catch (ZapExecutionException e) {
            return COMPLETED_PERCENTAGE;
        }
    }

    public void zapCrawlerSuccess() throws InterruptedException, ZapExecutionException {
        OffsetDateTime startedTime = OffsetDateTime.now();
        int timeoutSeconds = this.getZapTimeout();

        int status = zapCrawlerStatus();
        while (status < ZapDriver.COMPLETED_PERCENTAGE) {
            if (OffsetDateTime.now().isAfter(startedTime.plusSeconds(timeoutSeconds))) {
                throw new ZapExecutionException("ZAP Crawler failed to complete within the set timeout of " + timeoutSeconds + " seconds.");
            }

            status = zapCrawlerStatus();
            log("zap: Crawler progress is: " + status + "%");

            // Stop spamming ZAP with requests as soon as one completes. Status won't have changed in a short time & don't pause
            // when the scan is complete.
            if (status != ZapDriver.COMPLETED_PERCENTAGE) {
                TimeUnit.SECONDS.sleep(ZapDriver.ZAP_SCAN_SLEEP);
            }
        }
    }

    /**
     * Imports URLs from a text file
     *
     * @param path - The path to load from
     */
    public void importUrls(String path) throws ZapExecutionException {
        log("zap: Importing URLs from " + path);
        Map<String, String> arguments = Collections.singletonMap("filePath", path);

        JSONObject result = zapApi("importurls/action/importurls", arguments);

        verifyApiResultIsOk(result, "Request to import URLs returned a non-'OK' result.");
    }

    /**
     * Verifies ZAP session
     *
     * @param sessionPath - The path of the .session file
     */
    public void loadSession(String sessionPath) throws ZapExecutionException {
        log("zap: Loading session from " + sessionPath);
        Map<String, String> arguments = Collections.singletonMap("name", sessionPath);

        try {
            JSONObject result = zapApi("core/action/loadSession", arguments);


            verifyApiResultIsOk(result, "ZAP Session was empty, corrupt or non-existent.");
        } catch (Exception e) {
            throw new ZapExecutionException("Could not load session file.", e);
        }
    }

    /**
     * Loads a ZAP policy from a file path
     *
     * @param policy - The path to load from
     */
    public void loadPolicy(String policy) throws ZapExecutionException {
        Map<String, String> arguments = Collections.singletonMap("path", policy);
        JSONObject result = zapApi("ascan/action/importScanPolicy", arguments);

        boolean isOk = result.has("Result") && result.getString("Result").equals("OK");
        boolean alreadyExists = result.has("code") && result.getString("code").equals("already_exists");
        if (isOk || alreadyExists) {
            throw new ZapExecutionException("Request to import scan policy returned a non-'OK' result.");
        }
    }

    /**
     * Starts the ZAP attack. If allowedHosts is not provided in jenkinsfile, it will scan only hosts that are local
     *
     * @param zsp The parameters from the groovy step
     * @return Success
     */
    public boolean zapAttack(RunZapAttackStepParameters zsp) throws ZapExecutionException, URISyntaxException {
        // Reset scans
        startedScans.clear();

        JSONObject sitesObj = zapApi("core/view/sites");

        List<String> scanUrls = new ArrayList<>();
        JSONArray sites = sitesObj.getJSONArray("sites");
        for (Object site : sites) {
            String url = site.toString();

            // Only starts the scan if a scan on the site isn't currently running
            boolean found = scanUrls.stream().anyMatch(scan -> scan.equals(site.toString()));
            if (!found) {
                if (beginScan(url, zsp)) {
                    scanUrls.add(url);
                }
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
    private boolean beginScan(String url, RunZapAttackStepParameters zsp) throws URISyntaxException, ZapExecutionException {
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
                log(
                    "zap: Host " + host + " is not in the allowedHosts parameter and is not a local host. Not scanning.");
                return false;
            }
        }

        // Start the scan on a particular site with a particular user
        String attackUrl = "ascan/action/scan";
        Map<String, String> arguments = new HashMap<>();
        arguments.put("url", url);

        if (zsp.getUser() != 0) {
            log("zap: Loading user ID: " + zsp.getUser());
            attackUrl += "AsUser";
            arguments.put("userId", Integer.toString(zsp.getUser()));
        }

        if (zsp.getScanPolicyName() != null && !zsp.getScanPolicyName().isEmpty()) {
            arguments.put("scanPolicyName", zsp.getScanPolicyName());
        }

        JSONObject result = zapApi(attackUrl, arguments);
        int zapScanId = result.getInt("scan");
        startedScans.add(zapScanId);

        return true;
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
                int status = json.getInt("status");
                totalScanProgress += status;
            } catch (ZapExecutionException e) {
                totalScanProgress = COMPLETED_PERCENTAGE;
            }

            totalProgress += totalScanProgress;
        }

        return totalProgress / (totalScans);
    }

    /**
     * Starts the ZAP process
     *
     * @param zapHome - The location of the zap.sh file
     * @param ws - Passed by step
     * @param launcher - Passed by step
     */
    public void startZapProcess(String zapHome, FilePath ws, Launcher launcher) throws IOException {
        List<String> cmd = new ArrayList<>();

        Path zapPath = Paths.get(zapHome,
            launcher.isUnix() ? ZapDriverController.ZAP_UNIX_PROGRAM : ZapDriverController.ZAP_WIN_PROGRAM);
        cmd.add(zapPath.toString());

        cmd.add(ZapDriverController.CMD_DAEMON);

        cmd.add(ZapDriverController.CMD_HOST);
        cmd.add(zapHost);

        cmd.add(ZapDriverController.CMD_PORT);
        cmd.add(Integer.toString(zapPort));

        if (zapDir != null) {
            cmd.add(ZapDriverController.CMD_DIR);
            cmd.add(zapDir.getRemote());
        }

        cmd.add(ZapDriverController.CMD_CONFIG);
        cmd.add(ZapDriverController.CMD_DISABLEKEY);

        cmd.add(ZapDriverController.CMD_CONFIG);
        cmd.add(ZapDriverController.CMD_REGEX);

        cmd.add(ZapDriverController.CMD_CONFIG);
        cmd.add(ZapDriverController.CMD_NAME);

        cmd.add(ZapDriverController.CMD_CONFIG);
        cmd.add(ZapDriverController.CMD_TIMEOUT);

        for (String additionalConfiguration: additionalConfigurations) {
            cmd.add(ZapDriverController.CMD_CONFIG);
            cmd.add(additionalConfiguration);
        }

        if (rootCaFile != null) {
            cmd.add(ZapDriverController.CMD_CERTLOAD);
            cmd.add(rootCaFile);
        }

        launcher.launch().stdout(launcher.getListener().getLogger()).stderr(launcher.getListener().getLogger()).cmds(cmd).pwd(ws).start();
        launcher.getListener().getLogger().println("zap: Started successfully");
    }

    @Override
    public void enablePassiveScanners(List<Integer> ids) throws ZapExecutionException {
        zapApi("pscan/action/disableAllScanners/");

        String commaIds = String.join(",", ids
                .stream()
                .map(id -> id.toString())
                .collect(Collectors.toList()));
        Map<String, String> arguments = new HashMap<>();
        arguments.put("ids", commaIds);
        zapApi("pscan/action/enableScanners/", arguments);
    }

    @Override
    public void disablePassiveScanners(List<Integer> ids) throws ZapExecutionException {
        zapApi("pscan/action/enableAllScanners/");

        String commaIds = String.join(",", ids
                .stream()
                .map(id -> id.toString())
                .collect(Collectors.toList()));
        Map<String, String> arguments = new HashMap<>();
        arguments.put("ids", commaIds);
        zapApi("pscan/action/disableScanners/", arguments);
    }

    @Override
    public void zapAliveCheck() throws ZapExecutionException {
        OffsetDateTime startedTime = OffsetDateTime.now();
        while (!OffsetDateTime.now().isAfter(startedTime.plusSeconds(ZapDriver.ZAP_INITIALIZE_TIMEOUT))) {
            try {
                TimeUnit.SECONDS.sleep(ZapDriver.ZAP_INITIALIZE_WAIT);
                log("zap: Attempting to connect to ZAP at " + this.getZapHost() + ":" + this.getZapPort());

                new Socket(this.getZapHost(), this.getZapPort());
                return;
            } catch (IOException e) {
                log("zap: Zap connect attempt failed: " + e.getMessage());
                // Do nothing, just means ZAP hasn't started yet - we want to wait for the timeout
            } catch (InterruptedException e) {
                log("zap: Zap connect attempt failed: " + e.getMessage());
                break;
            }
        }

        throw new ZapExecutionException("Timed out waiting for ZAP application to become active for new connections.");
    }

    public String getZapReport() throws IOException, UnirestException, URISyntaxException {
        URI uri = new URI("http", null, zapHost, zapPort, "/OTHER/core/other/jsonreport", "formMethod=GET", null);

        InputStream response = Unirest.get(uri.toString()).asString().getRawBody();
        return IOUtils.toString(response, StandardCharsets.UTF_8);
    }

    public String getZapReportXML() throws IOException, UnirestException, URISyntaxException {
        URI uri = new URI("http", null, zapHost, zapPort, "/other/core/other/xmlreport", "formMethod=GET", null);

        InputStream response = Unirest.get(uri.toString()).asString().getRawBody();
        return IOUtils.toString(response, StandardCharsets.UTF_8);
    }

    public void setZapHost(String zapHost) {
        this.zapHost = zapHost;
    }

    public void setZapPort(int zapPort) {
        this.zapPort = zapPort;
    }

    @Override
    public void setZapDir(FilePath dir) {
        zapDir = dir;
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

    @Override
    public FilePath getZapDir() {
        return zapDir;
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

    /**
     * Gets the number of records the passive scanner still has to scan
     *
     * @return The number of records the passive scanner still has so scan
     */
    @Override
    public int zapRecordsToScan() throws ZapExecutionException {
        Map<String, String> arguments = Collections.emptyMap();
        JSONObject recordsToScan = zapApi("pscan/view/recordsToScan", arguments);
        return recordsToScan.getInt("recordsToScan");
    }

    @Override
    public void setZapRootCaFile(String rootCaFile) {
        this.rootCaFile = rootCaFile;
    }

    @Override
    public String getZapRootCaFile() {
        return rootCaFile;
    }

    @Override
    public void setAdditionalConfigurations(List<String> additionalConfigurations) {
        this.additionalConfigurations = additionalConfigurations;
    }

    @Override
    public List<String> getAdditionalConfigurations() {
        return additionalConfigurations;
    }

}
