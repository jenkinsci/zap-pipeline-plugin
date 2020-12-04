package com.vrondakis.zap;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.vrondakis.zap.workflow.RunZapAttackStepParameters;
import hudson.FilePath;
import hudson.Launcher;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

public interface ZapDriver {
    int COMPLETED_PERCENTAGE = 100;
    long ZAP_SCAN_SLEEP = 10;
    int ZAP_INITIALIZE_TIMEOUT = 100;
    int ZAP_INITIALIZE_WAIT = 20;

    boolean shutdownZap();

    boolean setZapMode(String zapMode);

    boolean startZapCrawler(String host);

    int zapCrawlerStatus();

    boolean importUrls(String path);

    boolean loadSession(String sessionPath);

    boolean loadPolicy(String policy);

    boolean zapAttack(RunZapAttackStepParameters zsp);

    boolean zapCrawlerSuccess();

    int zapAttackStatus();

    boolean startZapProcess(String zapHome, FilePath ws, Launcher launcher);

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

    boolean zapAliveCheck();
    
    int zapRecordsToScan();
}
