package com.webank.ai.fate.serving.core.async;

import com.alibaba.fastjson.JSON;
import com.lmax.disruptor.EventFactory;

public class AsyncMessageEvent<T> implements   Cloneable {

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
    @Override
    public  String toString(){
        return JSON.toJSONString(this);
    }

    @Override
    public AsyncMessageEvent clone() {
        AsyncMessageEvent stu = null;
        try{
            stu = (AsyncMessageEvent)super.clone();
        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return stu;
    }

    public static final EventFactory<AsyncMessageEvent> FACTORY = () -> new AsyncMessageEvent();

}
