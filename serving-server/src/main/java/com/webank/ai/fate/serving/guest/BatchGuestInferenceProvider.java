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

package com.webank.ai.fate.serving.guest;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.core.bean.BatchHostFederatedParams;
import com.webank.ai.fate.serving.core.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.BatchInferenceResult;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.model.ModelProcessor;
import com.webank.ai.fate.serving.core.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.core.rpc.core.FateService;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.rpc.FederatedRpcInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Description TODO
 * @Author kaideng
 **/
@FateService(name = "batchInferenece", preChain = {
//        "overloadMonitor",
        "guestBatchParamInterceptor",
//        "federationModelInterceptor",
        "guestModelInterceptor",
//        "federationRouterService"
        "federationRouterInterceptor"
}, postChain = {
        "defaultPostProcess"
})
@Service
public class BatchGuestInferenceProvider extends AbstractServiceAdaptor<BatchInferenceRequest, BatchInferenceResult> {

    @Autowired
    FederatedRpcInvoker federatedRpcInvoker;

    @Override
    public BatchInferenceResult doService(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) {

        Model model = context.getModel();

        Preconditions.checkArgument(model != null);
        /**
         * 用于替代原来的pipelineTask
         */
        ModelProcessor modelProcessor = model.getModelProcessor();


        BatchInferenceRequest batchInferenceRequest = (BatchInferenceRequest) inboundPackage.getBody();
        /**
         *  发往对端的参数
         */
        BatchHostFederatedParams batchHostFederatedParams = buildBatchHostFederatedParams(context, batchInferenceRequest);

        /**
         * guest 端与host同步预测，再合并结果
         */

        ListenableFuture<Proxy.Packet> originBatchResultFuture = federatedRpcInvoker.asyncBatch(context, batchHostFederatedParams);

        BatchInferenceResult batchFederatedResult = modelProcessor.guestBatchInference(context, batchInferenceRequest, originBatchResultFuture);

        return batchFederatedResult;
    }

    @Override
    protected BatchInferenceResult transformErrorMap(Context context, Map data) {
        return null;
    }
}
