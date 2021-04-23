package com.webank.ai.fate.serving.common.flow;


public class HealthInfo {
    private String component;
    private String host;
    private int port;
    private Object data;
    private long timeStamp;

    public HealthInfo(String component, String host, int port, long timeStamp) {
        this.component = component;
        this.host = host;
        this.port = port;
        this.timeStamp = timeStamp;
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

    public long getTimeStamp() {
        return timeStamp;
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

    public void setTimeStamp(long timeStamp) { this.timeStamp = timeStamp; }
}
