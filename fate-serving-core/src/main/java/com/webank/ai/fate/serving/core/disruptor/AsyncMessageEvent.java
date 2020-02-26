package com.webank.ai.fate.serving.core.disruptor;

import com.lmax.disruptor.EventFactory;
import lombok.Data;

@Data
public class AsyncMessageEvent<T> {

    private String name;

    private long timestamp;

    private String ip;

    private T data;

    void clear() {
        data = null;
    }

    public static final EventFactory<AsyncMessageEvent> FACTORY = () -> new AsyncMessageEvent();

}
