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

import com.lmax.disruptor.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisruptorExceptionHandler implements ExceptionHandler {

    private static Logger logger = LoggerFactory.getLogger(DisruptorExceptionHandler.class);

    @Override
    public void handleEventException(Throwable ex, long sequence, Object event) {
        logger.error("Disruptor event exception, {}", ex.getMessage());
        ex.printStackTrace();
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        logger.error("Disruptor start exception, {}", ex.getMessage());
        ex.printStackTrace();
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        logger.error("Disruptor shutdown exception, {}", ex.getMessage());
        ex.printStackTrace();
    }
}
