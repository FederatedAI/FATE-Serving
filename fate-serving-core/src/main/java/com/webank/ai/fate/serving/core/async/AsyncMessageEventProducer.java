package com.webank.ai.fate.serving.core.async;

import com.lmax.disruptor.EventTranslatorVararg;
import com.lmax.disruptor.RingBuffer;
import com.webank.ai.fate.serving.core.utils.GetSystemInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * Producer
 */
@Slf4j
public class AsyncMessageEventProducer {

    private final RingBuffer<AsyncMessageEvent> ringBuffer;

    public AsyncMessageEventProducer(RingBuffer<AsyncMessageEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public static final EventTranslatorVararg<AsyncMessageEvent> TRANSLATOR =
            (event, sequence, args) -> {
                event.setName(String.valueOf(args[0]));
                event.setAction(String.valueOf(args[1]));
                event.setData(args[2]);

                event.setIp(GetSystemInfo.getLocalIp());
                event.setTimestamp(System.currentTimeMillis());
            };

    public void publishEvent(Object... args) {
        ringBuffer.publishEvent(TRANSLATOR, args);
    }
}
