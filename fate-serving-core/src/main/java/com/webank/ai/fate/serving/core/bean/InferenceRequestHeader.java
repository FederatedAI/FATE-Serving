package com.webank.ai.fate.serving.core.bean;

public class InferenceRequestHeader {
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    String protocol;
}
