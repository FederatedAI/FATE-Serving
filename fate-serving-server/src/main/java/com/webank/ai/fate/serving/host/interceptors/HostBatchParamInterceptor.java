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
import com.webank.ai.fate.serving.core.bean.BatchHostFederatedParams;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.exceptions.HostInvalidParamException;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HostBatchParamInterceptor implements Interceptor {

    @Autowired
    private Environment environment;

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
        try {
            byte[] reqBody = (byte[]) inboundPackage.getBody();
            BatchHostFederatedParams batchHostFederatedParams = null;
            batchHostFederatedParams = JsonUtil.json2Object(reqBody, BatchHostFederatedParams.class);
            inboundPackage.setBody(batchHostFederatedParams);
            Preconditions.checkArgument(batchHostFederatedParams != null, "");
            Preconditions.checkArgument(batchHostFederatedParams.getBatchDataList() != null && batchHostFederatedParams.getBatchDataList().size() > 0);
            List<BatchHostFederatedParams.SingleInferenceData> datalist = batchHostFederatedParams.getBatchDataList();
            int batchSizeLimit = MetaInfo.BATCH_INFERENCE_MAX;
            Preconditions.checkArgument(datalist.size() <= batchSizeLimit, "batch size is big than " + batchSizeLimit);
        } catch (Exception e) {
            throw new HostInvalidParamException(e.getMessage());
        }
    }

}
