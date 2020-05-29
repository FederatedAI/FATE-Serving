package com.webank.ai.fate.serving.event;

public class CacheEventData {
    String key;
    Object data;
    public CacheEventData(String key, Object data) {
        this.key = key;
        this.data = data;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
