package com.webank.ai.fate.serving.common.flow;

public class HealthInfo {
    private String component;
    private String host;
    private int port;
    private Object data;

    public HealthInfo(String component, String host, int port) {
        this.component = component;
        this.host = host;
        this.port = port;
    }

    public Object getData() {
        return data;
    }

    public int getPort() {
        return port;
    }

    public String getComponent() {
        return component;
    }

    public String getHost() {
        return host;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
