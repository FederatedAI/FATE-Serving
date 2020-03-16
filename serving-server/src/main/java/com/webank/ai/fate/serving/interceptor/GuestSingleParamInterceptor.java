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

package com.webank.ai.fate.serving.interceptor;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.webank.ai.fate.serving.bean.InferenceRequest;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.Interceptor;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.utils.InferenceUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class GuestSingleParamInterceptor implements Interceptor {

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
        byte[] reqBody = (byte[]) inboundPackage.getBody();
        InferenceRequest inferenceRequest = null;
        try {
            inferenceRequest = JSON.parseObject(reqBody, InferenceRequest.class);
        } catch (Exception e) {
            throw new RuntimeException();
        }
        inboundPackage.setBody(inferenceRequest);
        Preconditions.checkArgument(inferenceRequest != null, "request message parse error");
        Preconditions.checkArgument(inferenceRequest.getFeatureData() != null, "no feature data");
        Preconditions.checkArgument(inferenceRequest.getSendToRemoteFeatureData() != null, "no send to remote feature data");
        Preconditions.checkArgument(StringUtils.isNotBlank(inferenceRequest.getServiceId()), "no service id");

        if (inferenceRequest.getCaseid().length() == 0) {
            inferenceRequest.setCaseId(InferenceUtils.generateCaseid());
        }

        context.setCaseId(inferenceRequest.getCaseid());
        context.setServiceId(inferenceRequest.getServiceId());
    }

}
