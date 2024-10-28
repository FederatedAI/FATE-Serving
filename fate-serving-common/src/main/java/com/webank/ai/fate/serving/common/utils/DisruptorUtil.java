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

package com.webank.ai.fate.serving.common.utils;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.webank.ai.fate.serving.common.async.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisruptorUtil {

    private static final Logger logger = LoggerFactory.getLogger(DisruptorUtil.class);

    private static final Disruptor<AsyncMessageEvent> disruptor;

    static {
        // Construct the Disruptor, use multi event publisher
        disruptor = new Disruptor(AsyncMessageEvent.FACTORY, 8192, DaemonThreadFactory.INSTANCE,
                ProducerType.MULTI, new YieldingWaitStrategy());

        // Connect the handler
        disruptor.handleEventsWith(new AsyncMessageEventHandler()).then(new ClearingEventHandler());

//        disruptor.handleEventsWithWorkerPool()

        // exception handler
        disruptor.setDefaultExceptionHandler(new DisruptorExceptionHandler());

        // Start the Disruptor, starts all threads running
        disruptor.start();

        logger.info("disruptor initialized");
    }

    /**
     * event producer
     * args[0] event name, e.g. interface name, use to @Subscribe value
     * args[1] event action
     * args[2] data params
     *
     * @param args
     */
    public static void producer(AsyncMessageEvent... args) {
        logger.info("Producer thread: {}", Thread.currentThread().getName());
        RingBuffer<AsyncMessageEvent> ringBuffer = disruptor.getRingBuffer();
        AsyncMessageEventProducer producer = new AsyncMessageEventProducer(ringBuffer);
        producer.publishEvent(args);
    }

    public static void shutdown() {
        if (disruptor != null) {
            disruptor.shutdown();
        }
    }
}
