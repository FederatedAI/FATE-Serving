package com.webank.ai.fate.serving.admin.bean;

import com.webank.ai.fate.register.common.RouterMode;

public enum  VerifyService {

//    PUBLISH_LOAD("publishLoad"),
//    PUBLISH_BIND("publishBind"),
//    PUBLISH_ONLINE("publishOnline"),
    INFERENCE("inference"),
    BATCH_INFERENCE("batchInference");

    private String callName;

    public String getCallName() {
        return callName;
    }

    public void setCallName(String callName) {
        this.callName = callName;
    }

    VerifyService(String callName) {
        this.callName = callName;
    }

    public static boolean contains(String callName) {
        VerifyService[] values = VerifyService.values();
        for (VerifyService value : values) {
            if (value.getCallName().equals(callName)) {
                return true;
            }
        }
        return false;
    }
}
