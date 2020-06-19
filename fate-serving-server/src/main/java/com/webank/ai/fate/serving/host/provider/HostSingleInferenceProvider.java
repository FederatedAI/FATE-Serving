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

package com.webank.ai.fate.serving.host.provider;

import com.webank.ai.fate.serving.common.bean.ServingServerContext;
import com.webank.ai.fate.serving.common.model.Model;
import com.webank.ai.fate.serving.common.model.ModelProcessor;
import com.webank.ai.fate.serving.common.rpc.core.*;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.InferenceRequest;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.guest.provider.AbstractServingServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@FateService(name = "hostInferenceProvider", preChain = {
        "requestOverloadBreaker",
        "hostParamInterceptor",
        "hostModelInterceptor",
        "hostSingleFeatureAdaptorInterceptor"
}, postChain = {

})
@Service
public class HostSingleInferenceProvider extends AbstractServingServiceProvider<InferenceRequest, ReturnResult> {

    private static final Logger logger = LoggerFactory.getLogger(HostSingleInferenceProvider.class);

    @Override
    protected OutboundPackage<ReturnResult> serviceFailInner(Context context, InboundPackage<InferenceRequest> data, Throwable e) {
        Map result = new HashMap(8);
        OutboundPackage<ReturnResult> outboundPackage = new OutboundPackage<ReturnResult>();
        ReturnResult returnResult = ErrorMessageUtil.handleExceptionToReturnResult(e);
        outboundPackage.setData(returnResult);
        context.setReturnCode(returnResult.getRetcode());
        return outboundPackage;
    }


    @FateServiceMethod(name = "federatedInference")
    public ReturnResult federatedInference(Context context, InboundPackage<InferenceRequest> data) {
        InferenceRequest params = data.getBody();
        Model model = ((ServingServerContext) context).getModel();
        ModelProcessor modelProcessor = model.getModelProcessor();
        ReturnResult result = modelProcessor.hostInference(context, params);
        return result;

    }

//    @FateServiceMethod(name = "federatedInference4Tree")
//    public ReturnResult federatedInference4Tree(Context context, InboundPackage<Map> data) {
//        Map params = data.getBody();
//        Model model = ((ServingServerContext) context).getModel();
//        Object componentObject = model.getModelProcessor().getComponent(params.get(Dict.COMPONENT_NAME).toString());
//        Preconditions.checkArgument(componentObject != null);
//        HeteroSecureBoostingTreeHost heteroSecureBoostingTreeHost = (HeteroSecureBoostingTreeHost) componentObject;
//        Map<String, Object> map = heteroSecureBoostingTreeHost.predictSingleRound(context, (Map<String, Object>) params.get(Dict.TREE_LOCATION));
//        ReturnResult result = new ReturnResult();
//        result.setRetcode(StatusCode.SUCCESS);
//        result.setData(map);
//        return result;
//    }


}