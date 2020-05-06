package com.webank.ai.fate.serving.core.async;

import com.lmax.disruptor.EventTranslatorVararg;
import com.lmax.disruptor.RingBuffer;
import com.webank.ai.fate.serving.core.utils.GetSystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Producer
 */
public class AsyncMessageEventProducer {

    private static Logger logger = LoggerFactory.getLogger(AsyncMessageEventProducer.class);

    private final RingBuffer<AsyncMessageEvent> ringBuffer;

    public AsyncMessageEventProducer(RingBuffer<AsyncMessageEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public static final EventTranslatorVararg<AsyncMessageEvent> TRANSLATOR =
            (event, sequence, args) -> {
//                event.setName(String.valueOf(args[0]));
//                event.setAction(String.valueOf(args[1]));
//                event.setData(args[2]);
//
//                event.setIp(GetSystemInfo.getLocalIp());
//                event.setTimestamp(System.currentTimeMillis());
                if (args[0] instanceof AsyncMessageEvent) {
                    AsyncMessageEvent argEvent = (AsyncMessageEvent) args[0];
                    event.setName(argEvent.getName());
                    event.setAction(argEvent.getAction());
                    event.setData(argEvent.getData());
                    event.setTimestamp(argEvent.getTimestamp());
                    if (event.getTimestamp() == 0) {
                        event.setTimestamp(System.currentTimeMillis());
                    }
                }
                event.setTimestamp(System.currentTimeMillis());
            };

    public void publishEvent(Object... args) {
        if(args!=null&&args.length>0) {
            ringBuffer.publishEvent(TRANSLATOR, args);
        }
    }
}
