package com.vrondakis.zap.workflow;

import java.util.Arrays;
import java.util.List;

public class ConfigurePassiveRulesStepParameters {

    private String action;
    private List<Integer> ids;

    public ConfigurePassiveRulesStepParameters(String action, Integer ... ids) {
        this.action = action;
        this.ids = Arrays.asList(ids);
    }

    public String getAction() {
        return this.action;
    }

    public List<Integer> getIds() {
        return this.ids;
    }
}
