package com.vrondakis.zap;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.vrondakis.zap.workflow.RunZapAttackStepParameters;
import hudson.FilePath;
import hudson.Launcher;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class ZapDriverStub implements ZapDriver {
    HashMap<Integer, Integer> failBuild = new HashMap<>();

    public ZapDriverStub(){
        super();
        failBuild.put(1, 0);
        failBuild.put(2, 9);
        failBuild.put(3, 0);
        failBuild.put(4, 0);
    }

    @Override
    public boolean shutdownZap() {
        return false;
    }

    @Override
    public boolean setZapMode(String zapMode) {
        return false;
    }

    @Override
    public boolean startZapCrawler(String host) {
        return false;
    }

    @Override
    public int zapCrawlerStatus() {
        return 0;
    }

    @Override
    public boolean importUrls(String path) {

        return false;
    }

    @Override
    public boolean loadSession(String sessionPath) {
        return false;
    }

    @Override
    public boolean loadPolicy(String policy) {
        return false;
    }

    @Override
    public boolean zapAttack(RunZapAttackStepParameters zsp) {
        return false;
    }

    @Override
    public int zapAttackStatus() {
        return 0;
    }

    @Override
    public boolean startZapProcess(String zapHome, FilePath ws, Launcher launcher) {
        return false;
    }

    @Override
    public void setZapHost(String zapHost) {

    }

    @Override
    public void setZapPort(int zapPort) {

    }

    @Override
    public void setFailBuild(int all, int high, int med, int low) {
        failBuild = new HashMap<>();
        failBuild.put(4, 0);
        failBuild.put(3, high);
        failBuild.put(2, med);
        failBuild.put(1, low);
    }

    @Override
    public void setZapTimeout(int timeout) {

    }

    @Override
    public void setAllowedHosts(List<String> allowedHosts) {

    }

    @Override
    public int getZapTimeout() {
        return 30;
    }

    @Override
    public int getZapPort() {
        return 1234;
    }

    @Override
    public HashMap<Integer, Integer> getFailBuild() {
        return failBuild;
    }

    @Override
    public String getZapHost() {
        return "localhost";
    }

    @Override
    public List<String> getAllowedHosts() {
        return null;
    }

    @Override
    public String getZapReport() throws IOException, UnirestException, URISyntaxException {
        URL url = Resources.getResource("zap-raw.json");
        return Resources.toString(url, Charsets.UTF_8);
    }
}
