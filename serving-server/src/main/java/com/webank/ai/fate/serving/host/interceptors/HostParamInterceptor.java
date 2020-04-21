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

package com.webank.ai.fate.serving.host.interceptors;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.InferenceRequest;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.Interceptor;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class HostParamInterceptor implements Interceptor {

    Logger logger = LoggerFactory.getLogger(HostParamInterceptor.class);
//    @Autowired
//    private Environment environment;

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
        logger.info("HostParamInterceptor doPreProcess");
        byte[] reqBody = (byte[]) inboundPackage.getBody();

        if (context.getActionType().equalsIgnoreCase(Dict.FEDERATED_INFERENCE_FOR_TREE)) {
            Map params = JSON.parseObject(reqBody, HashMap.class);
            Preconditions.checkArgument(params != null, "parse inference params error");
            inboundPackage.setBody(params);
//            context.setCaseId(inferenceRequest.getCaseid());
        } else {
            InferenceRequest inferenceRequest = null;
            try {
                inferenceRequest = JSON.parseObject(reqBody, InferenceRequest.class);
            } catch (Exception e) {
                throw new RuntimeException();
            }
            Preconditions.checkArgument(inferenceRequest != null, "parse inference params error");
            inboundPackage.setBody(inferenceRequest);

//            context.setCaseId(inferenceRequest.getCaseid());
        }


    }

}
