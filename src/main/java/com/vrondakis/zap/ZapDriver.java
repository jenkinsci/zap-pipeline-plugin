package com.vrondakis.zap;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.vrondakis.zap.workflow.RunZapAttackStepParameters;
import com.vrondakis.zap.workflow.RunZapCrawlerParameters;
import hudson.FilePath;
import hudson.Launcher;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

public interface ZapDriver {
    int COMPLETED_PERCENTAGE = 100;
    long ZAP_SCAN_SLEEP = 10;
    long ZAP_SCAN_STATUS_PRINT_INTERVAL = 60;
    int ZAP_INITIALIZE_TIMEOUT = 100;
    int ZAP_INITIALIZE_WAIT = 20;

    void setLogger(PrintStream logger);

    void shutdownZap() throws ZapExecutionException;

    void setZapMode(String zapMode) throws ZapExecutionException;

    void startZapCrawler(RunZapCrawlerParameters zcp) throws ZapExecutionException, ZapExecutionException;

    int zapCrawlerStatus();

    void importUrls(String path) throws ZapExecutionException;

    void loadSession(String sessionPath) throws ZapExecutionException;

    void loadPolicy(String policy) throws ZapExecutionException;

    boolean zapAttack(RunZapAttackStepParameters zsp) throws ZapExecutionException, UnirestException, IOException, URISyntaxException;

    void zapCrawlerSuccess() throws InterruptedException, ZapExecutionException;

    int zapAttackStatus();

    void startZapProcess(String zapHome, FilePath ws, Launcher launcher) throws IOException;

    void enablePassiveScanners(List<Integer> ids) throws ZapExecutionException;

    void disablePassiveScanners(List<Integer> ids) throws ZapExecutionException;

    void setZapHost(String zapHost);

    void setZapPort(int zapPort);

    void setZapDir(FilePath dir);

    void setFailBuild(int all, int hihg, int med, int low);

    void setZapTimeout(int timeout);

    void setAllowedHosts(List<String> allowedHosts);

    int getZapTimeout();

    int getZapPort();

    FilePath getZapDir();

    HashMap<Integer, Integer> getFailBuild();

    String getZapHost();

    List<String> getAllowedHosts();

    String getZapReport() throws IOException, UnirestException, URISyntaxException;
    String getZapReportXML() throws IOException, UnirestException, URISyntaxException;

    void zapAliveCheck() throws ZapExecutionException;

    void setZapRootCaFile(String rootCaFile);

    String getZapRootCaFile();

    void setAdditionalConfigurations(List<String> additionalConfigurations);

    List<String> getAdditionalConfigurations();

    int zapRecordsToScan() throws ZapExecutionException;

    List<PluginProgress> zapAttackProgress();
}
