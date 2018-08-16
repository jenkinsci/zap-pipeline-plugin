package com.barracuda.zapcomp.workflow;

public class RunZapCrawlerParameters {
    private String host;

    public RunZapCrawlerParameters(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }
}