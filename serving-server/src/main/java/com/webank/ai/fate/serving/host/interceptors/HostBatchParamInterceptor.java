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
import com.webank.ai.fate.serving.core.bean.BatchHostFederatedParams;
import com.webank.ai.fate.serving.core.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.Interceptor;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
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

        byte[]  reqBody = (byte[])inboundPackage.getBody();
        BatchHostFederatedParams batchHostFederatedParams =null;
        try {
            batchHostFederatedParams = JSON.parseObject(reqBody, BatchInferenceRequest.class);
        }catch(Exception e){
            throw new  RuntimeException();
        }
        inboundPackage.setBody(batchHostFederatedParams);
        Preconditions.checkArgument(batchHostFederatedParams!=null,"");
        Preconditions.checkArgument(batchHostFederatedParams.getBatchDataList()!=null);
        List<BatchHostFederatedParams.SingleInferenceData>  datalist = batchHostFederatedParams.getBatchDataList();
        int  batchSizeLimit = environment.getProperty("batch.inference.max", int.class, 50);
        Preconditions.checkArgument(datalist.size()<=batchSizeLimit);
//        for(BatchInferenceRequest.SingleInferenceData  singleInferenceData: datalist){
//            singleInferenceData.
//        }

    }

}
