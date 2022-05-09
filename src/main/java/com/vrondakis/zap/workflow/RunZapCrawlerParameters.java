package com.vrondakis.zap.workflow;

public class RunZapCrawlerParameters {
    private String host;
    private int maxChildren;
    private String contextName;
    private int contextId;
    private boolean subtreeOnly;
    private boolean recurse;
    private int userId;

    public RunZapCrawlerParameters(String host, int maxChildren, String contextName, int contextId, boolean subtreeOnly, boolean recurse, int userId) {
        this.host = host;
        this.maxChildren = maxChildren;
        this.contextName = contextName;
        this.contextId = contextId;
        this.subtreeOnly = subtreeOnly;
        this.recurse = recurse;
        this.userId = userId;
    }

    public String getHost() {
        return host;
    }

    public int getMaxChildren() {
        return maxChildren;
    }

    public String getContextName() {
        return contextName;
    }

    public int getContextId() {
        return contextId;
    }

    public boolean getSubtreeOnly() {
        return subtreeOnly;
    }

    public boolean getRecurse() {
        return recurse;
    }

    public int getUserId() {
        return userId;
    }
}