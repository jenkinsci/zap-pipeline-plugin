package com.barracuda.zapcomp;

import com.barracuda.zapcomp.workflow.RunZapAttackStepParameters;
import com.mashape.unirest.http.*;
import com.mashape.unirest.http.exceptions.*;
import hudson.*;
import net.sf.json.*;
import org.apache.commons.io.*;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * ZapDriver Controls ZAP using HTTP api
 *
 * @see <a href="https://github.com/zaproxy/zaproxy/wiki/ApiDetails">ZAP API Documentation</a>
 */

public class ZapDriver {
    private static String ZAP_HOST;
    private static int ZAP_PORT;
    private static int ZAP_TIMEOUT;
    private static int FAIL_BUILD;

    private static final String CMD_DAEMON = "-daemon";
    private static final String CMD_HOST = "-host";
    private static final String CMD_PORT = "-port";
    private static final String CMD_CONFIG = "-config";
    private static final String CMD_DISABLEKEY = "api.disablekey=true";
    private static final String CMD_REGEX = "api.addrs.addr.regex=true";
    private static final String CMD_NAME = "api.addrs.addr.name=10.*";
    private static final String CMD_TIMEOUT = "connection.timeoutInSecs=600";

    private static final String ZAP_UNIX_PROGRAM = "zap.sh";
    private static final String ZAP_WIN_PROGRAM = "zap.bat";
    private static List<String> ALLOWED_HOSTS = new ArrayList<>();
    private static final List<Integer> STARTED_SCANS = new ArrayList<>();
    private static int crawlId;

    /**
     * Calls the ZAP api
     *
     * @param apiUrl The API endpoint to call
     * @param params Map with GET Parameters for the call
     * @return JSONObject or null
     */
    private static JSONObject zapApi(String apiUrl, Map<String, String> params) {
        try {
            String query = formatParams(params);

            System.out.println(apiUrl);


            apiUrl = "/JSON/" + apiUrl;
            URI uri = new URI("http", null, ZAP_HOST, ZAP_PORT, apiUrl, query, null);

            InputStream response = Unirest.get(uri.toString()).asString().getRawBody();

            String res = IOUtils.toString(response, StandardCharsets.UTF_8);
            System.out.println(res);
            return JSONObject.fromObject(res);
        } catch (URISyntaxException | IOException | UnirestException e) {
            // Should be handled in calling function
            e.printStackTrace();
            return null;
        }
    }

    // Converts map of parameters to URL parameters
    private static String formatParams(Map<String, String> params) {
        return params.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("&"));
    }

    private static JSONObject zapApi(String apiUrl) {
        return zapApi(apiUrl, Collections.emptyMap());
    }

    public static boolean shutdownZap() {
        return zapApi("core/action/shutdown") != null;
    }

    public static boolean setZapMode(String mode) {
        Map<String, String> arguments = Collections.singletonMap("mode", mode);
        return zapApi("core/action/setMode", arguments) != null;
    }

    /**
     * Starts the ZAP crawler on a specified URL
     *
     * @param host The host to attack
     * @return Success
     */
    public static boolean startZapCrawler(String host) {
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
    public static int zapCrawlerStatus() {
        Map<String, String> arguments = Collections.singletonMap("scanId", Integer.toString(crawlId));
        try {
            JSONObject json = zapApi("spider/view/status", arguments);
            if (json != null) {
                return json.getInt("status");
            } else {
                return Constants.COMPLETED_PERCENTAGE; // Failed to retrieve status so skip it
            }

        } catch (JSONException e) {
            return Constants.COMPLETED_PERCENTAGE;
        }
    }

    /**
     * Imports URLs from a text file
     *
     * @param path - The path to load from
     * @return Success
     */
    public static boolean importUrls(String path) {
        System.out.println("zap-comp: Importing URLs from " + path);
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
    public static boolean loadSession(String sessionPath) {
        System.out.println("zap-comp: Loading session from " + sessionPath);
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
    public static boolean loadPolicy(String policy) {
        Map<String, String> arguments = Collections.singletonMap("path", policy);
        JSONObject result = zapApi("ascan/action/importScanPolicy", arguments);

        if (result == null) return false;

        return (result.has("Result") && result.getString("Result").equals("OK")) ||
                (result.has("code") && result.getString("code").equals("already_exists"));
    }

    /**
     * Starts the ZAP attack. If allowedHosts is not provided in jenkinsfile, it will scan only hosts that are local
     *
     * @param zsp The parameters from the groovy step
     * @return Success
     */
    public static boolean zapAttack(RunZapAttackStepParameters zsp) {
        // Reset scans
        STARTED_SCANS.clear();

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
     * Begins a scan on a selected URL if it is in the allowed hosts parameter or if it is local (and allowed hosts parameter is empty)
     *
     * @param url The URL to scan. Does not include ZAP host prefix
     * @return Success
     */
    private static boolean beginScan(String url, RunZapAttackStepParameters zsp) {
        try {
            List<String> allowedHosts = ZapDriver.ALLOWED_HOSTS;
            String host = new URI(url).getHost(); // http://10.0.0.1 becomes 10.0.0.1


            // If it is in the allowed hosts parameter - or if the url is unset if it is local
            // localhost.localdomain does not resolve properly with INetAddress.getByName, which is why there is an additional check
            if (!host.equals("localhost.localdomain")) {
                if (ZapDriver.ALLOWED_HOSTS.isEmpty()) {
                    InetAddress addr = null;
                    try {
                        addr = InetAddress.getByName(host);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (addr == null) return false;

                    if (!addr.isAnyLocalAddress() && !addr.isLoopbackAddress())
                        return false;
                } else if (!allowedHosts.contains(host)) {
                    System.out.println("Host " + host + " is not in the allowedHosts parameter and is not a local host. Not scanning.");
                    return false;
                }
            }
            // Start the scan on a particular site with a particular user
            String attackUrl = "ascan/action/scan";
            Map<String, String> arguments = new HashMap<>();
            arguments.put("url", url);

            if (zsp.getUser() != 0) {
                System.out.println("zap-comp: Loading user ID: " + zsp.getUser());
                attackUrl += "AsUser";
                arguments.put("userId", Integer.toString(zsp.getUser()));
            }

            if (zsp.getScanPolicyName() != null && !zsp.getScanPolicyName().isEmpty()) {
                arguments.put("scanPolicyName", zsp.getScanPolicyName());
            }

            JSONObject result = zapApi(attackUrl, arguments);
            if (result != null) {
                int zapScanId = result.getInt("scan");
                STARTED_SCANS.add(zapScanId);
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
    public static int zapAttackStatus() {
        int totalScans = STARTED_SCANS.size();
        int totalProgress = 0;

        if (STARTED_SCANS.isEmpty()) {
            // Called but no scans running
            return Constants.COMPLETED_PERCENTAGE;
        }

        for (Integer startedScan : STARTED_SCANS) {
            int totalScanProgress = 0;

            Map<String, String> arguments = Collections.singletonMap("scanId", Integer.toString(startedScan));
            try {
                JSONObject json = zapApi("ascan/view/status", arguments);
                if (json != null) {
                    int status = json.getInt("status");
                    totalScanProgress += status;
                } else {
                    totalScanProgress = Constants.COMPLETED_PERCENTAGE; // Failed to retrieve status so skip it
                }
            } catch (JSONException e) {
                totalScanProgress = Constants.COMPLETED_PERCENTAGE;
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
    public static boolean startZapProcess(String zapHome, FilePath ws, Launcher launcher) {
        List<String> cmd = new ArrayList<>();

        Path zapPath = Paths.get(zapHome, launcher.isUnix() ? ZAP_UNIX_PROGRAM : ZAP_WIN_PROGRAM);
        cmd.add(zapPath.toString());

        cmd.add(CMD_DAEMON);

        cmd.add(CMD_HOST);
        cmd.add(ZAP_HOST);

        cmd.add(CMD_PORT);
        cmd.add(Integer.toString(ZAP_PORT));

        cmd.add(CMD_CONFIG);
        cmd.add(CMD_DISABLEKEY);

        cmd.add(CMD_CONFIG);
        cmd.add(CMD_REGEX);

        cmd.add(CMD_CONFIG);
        cmd.add(CMD_NAME);

        cmd.add(CMD_CONFIG);
        cmd.add(CMD_TIMEOUT);

        try {
            launcher.launch().cmds(cmd).pwd(ws).start();
            System.out.println("zap-comp: Started successfully");
            return true;
        } catch (Exception e) {
            System.out.println("zap-comp: An error occured while staring ZAP");
            e.printStackTrace();
            return false;
        }
    }

    public static void setZapHost(String zapHost) {
        ZapDriver.ZAP_HOST = zapHost;
    }
    public static void setZapPort(int zapPort) {
        ZapDriver.ZAP_PORT = zapPort;
    }
    public static void setFailBuild(int fail) {
        ZapDriver.FAIL_BUILD = fail;
    }
    public static void setZapTimeout(int timeout) {
        ZapDriver.ZAP_TIMEOUT = timeout;
    }
    public static void setAllowedHosts(List<String> allowedHosts) {
        ZapDriver.ALLOWED_HOSTS = allowedHosts;
    }
    public static int getZapTimeout() {
        return ZapDriver.ZAP_TIMEOUT;
    }
    public static int getZapPort() {
        return ZapDriver.ZAP_PORT;
    }
    public static int getFailBuild() {
        return ZapDriver.FAIL_BUILD;
    }
    public static String getZapHost() {
        return ZapDriver.ZAP_HOST;
    }
    public static List<String> getAllowedHosts() {
        return ZapDriver.ALLOWED_HOSTS;
    }
}