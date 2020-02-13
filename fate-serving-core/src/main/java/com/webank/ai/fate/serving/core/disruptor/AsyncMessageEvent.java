package com.webank.ai.fate.serving.core.disruptor;

import com.alibaba.fastjson.JSONObject;
import com.lmax.disruptor.EventFactory;

public class AsyncMessageEvent<T> {

    private String name;

    private T data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setData(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public AsyncMessageEvent() {
    }

    void clear() {
        data = null;
    }

    public static final EventFactory<AsyncMessageEvent> FACTORY = () -> new AsyncMessageEvent();

    @Override
    public String toString() {
        return "AsyncMessageEvent{" +
                "name='" + name + '\'' +
                ", data=" + JSONObject.toJSONString(data) +
                '}';
    }
}
