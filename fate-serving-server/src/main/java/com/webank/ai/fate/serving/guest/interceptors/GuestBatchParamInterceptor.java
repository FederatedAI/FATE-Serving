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

package com.webank.ai.fate.serving.guest.interceptors;

import com.google.common.base.Preconditions;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.Interceptor;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.exceptions.GuestInvalidParamException;
import com.webank.ai.fate.serving.core.utils.InferenceUtils;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GuestBatchParamInterceptor implements Interceptor {

    Logger logger = LoggerFactory.getLogger(GuestBatchParamInterceptor.class);

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
        InferenceServiceProto.InferenceMessage message = (InferenceServiceProto.InferenceMessage) inboundPackage.getBody();
        BatchInferenceRequest batchInferenceRequest = null;
        try {
            batchInferenceRequest = JsonUtil.json2Object(message.getBody().toByteArray(), BatchInferenceRequest.class);
            inboundPackage.setBody(batchInferenceRequest);
            Preconditions.checkArgument(batchInferenceRequest != null, "request message parse error");
            Preconditions.checkArgument(StringUtils.isNotBlank(batchInferenceRequest.getServiceId()), "no service id");
            if (batchInferenceRequest.getCaseid() == null || batchInferenceRequest.getCaseid().length() == 0) {
                batchInferenceRequest.setCaseId(InferenceUtils.generateCaseid());
            }
            context.setCaseId(batchInferenceRequest.getCaseid());
            context.setServiceId(batchInferenceRequest.getServiceId());
            if (batchInferenceRequest.getApplyId() == null) {
                context.setApplyId(batchInferenceRequest.getApplyId());
            }
        } catch (Exception e) {
            throw new GuestInvalidParamException(e.getMessage());
        }

    }

}
