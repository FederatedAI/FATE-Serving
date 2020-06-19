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

package com.webank.ai.fate.serving.common.interceptors;

import com.webank.ai.fate.serving.common.flow.FlowCounterManager;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.Interceptor;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.exceptions.OverLoadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description TODO
 * @Author
 **/
@Service
public class RequestOverloadBreaker implements Interceptor {
    Logger logger = LoggerFactory.getLogger(RequestOverloadBreaker.class);

    @Autowired
    FlowCounterManager flowCounterManager;

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
        String resource = context.getResourceName();
        boolean pass = flowCounterManager.pass(resource);
        if (!pass) {
            flowCounterManager.block(context.getServiceName());
            logger.warn("request was block by over load, service name: {}", context.getServiceName());
            throw new OverLoadException("request was block by over load, service name: " + context.getServiceName());
        }
    }


}
