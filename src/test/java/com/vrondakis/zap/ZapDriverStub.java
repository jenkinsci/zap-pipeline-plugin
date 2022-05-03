package com.vrondakis.zap;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.vrondakis.zap.workflow.RunZapAttackStepParameters;
import com.vrondakis.zap.workflow.RunZapCrawlerParameters;
import hudson.FilePath;
import hudson.Launcher;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ZapDriverStub implements ZapDriver {
    private int port;
    private int timeout;
    private String host;
    private FilePath dir;
    private String loadedSessionPath = "";
    private List<String> allowedHosts;
    private HashMap<Integer, Integer> failBuild = new HashMap<>();

    private String rootCaFile;
    private List<String> additionalConfigurations;

    boolean zapWasShutdown = false;
    private List<Integer> passiveRulesIds = new ArrayList<>();
    private String passiveRulesAction;
    private PrintStream logger = System.out;

    /**
     * Sets the current logger.
     * @param logger
     */
    public void setLogger(PrintStream logger) {
        this.logger = logger;
    }

    public ZapDriverStub() {
        super();
    }

    public String getLoadedSessionPath() {
        return loadedSessionPath;
    }

    @Override
    public void shutdownZap() {
        zapWasShutdown = true;
    }

    public boolean isZapShutdown() {
        return zapWasShutdown;
    }

    @Override
    public void setZapMode(String zapMode) {
        // Do nothing
    }

    @Override
    public void startZapCrawler(RunZapCrawlerParameters zcp) {
        // Do nothing
    }

    @Override
    public int zapCrawlerStatus() {
        return 100;
    }

    @Override
    public void importUrls(String path) {
        // Do nothing
    }

    @Override
    public void loadSession(String sessionPath) {
        loadedSessionPath = sessionPath;
    }

    @Override
    public void loadPolicy(String policy) {
        // Do nothing
    }

    @Override
    public boolean zapAttack(RunZapAttackStepParameters zsp) {
        return false;
    }

    @Override
    public void zapCrawlerSuccess() {
        // Do nothing
    }

    @Override
    public int zapAttackStatus() {
        return 0;
    }

    @Override
    public void startZapProcess(String zapHome, FilePath ws, Launcher launcher) {
        // do nothing
    }

    @Override
    public void enablePassiveScanners(List<Integer> ids) throws ZapExecutionException {
        passiveRulesIds = ids;
        passiveRulesAction = "enable";
    }

    @Override
    public void disablePassiveScanners(List<Integer> ids) throws ZapExecutionException {
        passiveRulesIds = ids;
        passiveRulesAction = "disable";
    }

    public List<Integer> getPassiveRulesIds() {
        return passiveRulesIds;
    }

    public String getPassiveRulesAction() {
        return passiveRulesAction;
    }

    @Override
    public void setZapHost(String zapHost) {
        host = zapHost;
    }

    @Override
    public void setZapPort(int zapPort) {
        port = zapPort;
    }

    @Override
    public void setZapDir(FilePath dir) {
        this.dir = dir;
    }

    @Override
    public void setFailBuild(int all, int high, int med, int low) {
        failBuild.put(ZapArchive.ALL_ALERT, all);
        failBuild.put(ZapArchive.HIGH_ALERT, high);
        failBuild.put(ZapArchive.MEDIUM_ALERT, med);
        failBuild.put(ZapArchive.LOW_ALERT, low);
    }

    @Override
    public void setZapTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void setAllowedHosts(List<String> allowedHosts) {
        this.allowedHosts = allowedHosts;
    }

    @Override
    public int getZapTimeout() {
        return timeout;
    }

    @Override
    public int getZapPort() {
        return port;
    }

    @Override
    public FilePath getZapDir() {
        return dir;
    }

    @Override
    public HashMap<Integer, Integer> getFailBuild() {
        return failBuild;
    }

    @Override
    public String getZapHost() {
        return host;
    }

    @Override
    public List<String> getAllowedHosts() {
        return this.allowedHosts;
    }

    @Override
    public String getZapReport() throws IOException {
        URL url = Resources.getResource("zap-raw.json");
        return Resources.toString(url, Charsets.UTF_8);
    }

    @Override
    public String getZapReportXML() throws IOException {
        URL url = Resources.getResource("zap-raw.xml");
        return Resources.toString(url, Charsets.UTF_8);
    }

    @Override
    public void zapAliveCheck() {
        // Do nothing
    }

    @Override
    public int zapRecordsToScan() {
        return 0;
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
