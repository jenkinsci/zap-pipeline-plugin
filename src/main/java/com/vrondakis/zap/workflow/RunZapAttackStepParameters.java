package com.vrondakis.zap.workflow;

public class RunZapAttackStepParameters {
    private String scanPolicyName;
    private int user;
    private int contextId;
    private boolean recurse;
    private boolean inScopeOnly;
    private String method;
    private String postData;

    public RunZapAttackStepParameters(String scanPolicyName, int user, int contextId, boolean recurse, boolean inScopeOnly, String method, String postData) {
        this.scanPolicyName = scanPolicyName;
        this.user = user;
        this.contextId = contextId;
        this.recurse = recurse;
        this.inScopeOnly = inScopeOnly;
        this.method = method;
        this.postData = postData;
    }

    public String getScanPolicyName() {
        return scanPolicyName;
    }

    public int getUser() {
        return user;
    }

    public int getContextId() {
        return contextId;
    }

    public boolean getRecurse() {
        return recurse;
    }

    public boolean getInScopeOnly() {
        return inScopeOnly;
    }

    public String getMethod() {
        return method;
    }

    public String getPostData() {
        return postData;
    }
}