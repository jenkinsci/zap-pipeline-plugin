package com.barracuda.zapcomp.workflow;

public class RunZapAttackStepParameters {
    private String filePath;

    public RunZapAttackStepParameters(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath(){
        return this.filePath;
    }
}