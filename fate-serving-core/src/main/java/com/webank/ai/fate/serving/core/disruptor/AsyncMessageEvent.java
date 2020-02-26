package com.webank.ai.fate.serving.core.disruptor;

import com.lmax.disruptor.EventFactory;
import lombok.Data;

@Data
public class AsyncMessageEvent<T> {

    /**
     * event name, e.g. interface name, use to @Subscribe value
     */
    private String name;

    private String action;

    private T data;

    private String ip;

    private long timestamp;

    void clear() {
        this.name = null;
        this.action = null;
        this.data = null;
        this.ip = null;
        this.timestamp = 0;
    }

    public static final EventFactory<AsyncMessageEvent> FACTORY = () -> new AsyncMessageEvent();

}
