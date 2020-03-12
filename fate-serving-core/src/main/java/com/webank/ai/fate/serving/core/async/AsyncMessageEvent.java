package com.webank.ai.fate.serving.core.async;

import com.lmax.disruptor.EventFactory;

public class AsyncMessageEvent<T> {

    /**
     * event name, e.g. interface name, use to @Subscribe value
     */
    private String name;

    private String action;

    private T data;

    private String ip;

    private long timestamp;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    void clear() {
        this.name = null;
        this.action = null;
        this.data = null;
        this.ip = null;
        this.timestamp = 0;
    }

    public static final EventFactory<AsyncMessageEvent> FACTORY = () -> new AsyncMessageEvent();

}
