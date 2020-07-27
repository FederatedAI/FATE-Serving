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

import com.google.common.base.Preconditions;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.Interceptor;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.InferenceRequest;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class HostParamInterceptor implements Interceptor {
    Logger logger = LoggerFactory.getLogger(HostParamInterceptor.class);

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
        byte[] reqBody = (byte[]) inboundPackage.getBody();
        if (context.getActionType().equalsIgnoreCase(Dict.FEDERATED_INFERENCE_FOR_TREE)) {
            Map params = JsonUtil.json2Object(reqBody, HashMap.class);
            Preconditions.checkArgument(params != null, "parse inference params error");
            inboundPackage.setBody(params);
        } else {
            InferenceRequest inferenceRequest = JsonUtil.json2Object(reqBody, InferenceRequest.class);
            if (StringUtils.isBlank(context.getVersion()) || Long.parseLong(context.getVersion()) < 200) {
                Map hostParams = JsonUtil.json2Object(reqBody, Map.class);
                Preconditions.checkArgument(hostParams != null, "parse inference params error");
                Preconditions.checkArgument(hostParams.get("featureIdMap") != null, "parse inference params featureIdMap error");
                inferenceRequest.getSendToRemoteFeatureData().putAll((Map) hostParams.get("featureIdMap"));
            }
            Preconditions.checkArgument(inferenceRequest != null, "parse inference params error");
            inboundPackage.setBody(inferenceRequest);
        }
    }

}
