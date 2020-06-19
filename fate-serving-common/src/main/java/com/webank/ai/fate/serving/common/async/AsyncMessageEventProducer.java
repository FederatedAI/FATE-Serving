/*
 * Copyright 2019 The FATE Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.ai.fate.serving.common.async;

import com.lmax.disruptor.EventTranslatorVararg;
import com.lmax.disruptor.RingBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Producer
 */
public class AsyncMessageEventProducer {

    public static final EventTranslatorVararg<AsyncMessageEvent> TRANSLATOR =
            (event, sequence, args) -> {
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
    private static Logger logger = LoggerFactory.getLogger(AsyncMessageEventProducer.class);
    private final RingBuffer<AsyncMessageEvent> ringBuffer;

    public AsyncMessageEventProducer(RingBuffer<AsyncMessageEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void publishEvent(Object... args) {
        if (args != null && args.length > 0) {
            ringBuffer.tryPublishEvent(TRANSLATOR, args);
        }
    }
}
