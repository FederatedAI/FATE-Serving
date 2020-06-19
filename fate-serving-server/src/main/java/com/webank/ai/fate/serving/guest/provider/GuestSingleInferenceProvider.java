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

package com.webank.ai.fate.serving.guest.provider;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.serving.common.bean.ServingServerContext;
import com.webank.ai.fate.serving.common.model.Model;
import com.webank.ai.fate.serving.common.model.ModelProcessor;
import com.webank.ai.fate.serving.common.rpc.core.*;
import com.webank.ai.fate.serving.core.bean.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

@FateService(name = "singleInference", preChain = {
        "requestOverloadBreaker",
        "guestSingleParamInterceptor",
        "guestModelInterceptor",
        "federationRouterInterceptor"
}, postChain = {

})
@Service
public class GuestSingleInferenceProvider extends AbstractServingServiceProvider<InferenceRequest, ReturnResult> {
    @Autowired
    FederatedRpcInvoker federatedRpcInvoker;

    @Override
    public ReturnResult doService(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) {
        Model model = ((ServingServerContext) context).getModel();
        ModelProcessor modelProcessor = model.getModelProcessor();
        InferenceRequest inferenceRequest = (InferenceRequest) inboundPackage.getBody();
        Map<String, Future> futureMap = Maps.newHashMap();
        List<FederatedRpcInvoker.RpcDataWraper> rpcList = this.buildRpcDataWraper(context, Dict.FEDERATED_INFERENCE, inferenceRequest);
        rpcList.forEach((rpcDataWraper -> {
            ListenableFuture<ReturnResult> future = federatedRpcInvoker.singleInferenceRpcWithCache(context, rpcDataWraper, MetaInfo.PROPERTY_REMOTE_MODEL_INFERENCE_RESULT_CACHE_SWITCH);
            futureMap.put(rpcDataWraper.getHostModel().getPartId(), future);
        }));
        ReturnResult returnResult = modelProcessor.guestInference(context, inferenceRequest, futureMap, MetaInfo.SINGLE_INFERENCE_RPC_TIMEOUT);
        return returnResult;
    }

    @Override
    protected OutboundPackage<ReturnResult> serviceFailInner(Context context, InboundPackage<InferenceRequest> data, Throwable e) {
        OutboundPackage<ReturnResult> outboundPackage = new OutboundPackage<ReturnResult>();
        ReturnResult returnResult = ErrorMessageUtil.handleExceptionToReturnResult(e);
        outboundPackage.setData(returnResult);
        context.setReturnCode(returnResult.getRetcode());
        return outboundPackage;
    }
}
