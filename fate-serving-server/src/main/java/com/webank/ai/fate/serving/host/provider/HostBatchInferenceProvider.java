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
import com.webank.ai.fate.serving.common.interfaces.CustomInterfaceInstanceManager;
import com.webank.ai.fate.serving.common.interfaces.CustomPreprocessHandle;
import com.webank.ai.fate.serving.common.model.Model;
import com.webank.ai.fate.serving.common.rpc.core.FateService;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.BaseException;
import com.webank.ai.fate.serving.guest.provider.AbstractServingServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@FateService(name = "batchInference", preChain = {
        "requestOverloadBreaker",
        "hostBatchParamInterceptor",
        "hostModelInterceptor",
        "hostBatchFeatureAdaptorInterceptor"
}, postChain = {

})
@Service
public class HostBatchInferenceProvider extends AbstractServingServiceProvider<BatchInferenceRequest, BatchInferenceResult> {

    private static final Logger logger = LoggerFactory.getLogger(HostBatchInferenceProvider.class);

    @Override
    public BatchInferenceResult doService(Context context, InboundPackage data, OutboundPackage outboundPackage) {
        Object requestInPreInterface = CustomInterfaceInstanceManager.getInstanceForName(MetaInfo.PROPERTY_INTERFACE_BATCH_HOST_PREREQUEST);
        if(requestInPreInterface != null){
            CustomPreprocessHandle<InboundPackage> requestInPreHandle = (CustomPreprocessHandle<InboundPackage>)requestInPreInterface;
            requestInPreHandle.handle(context,data);
        }
        BatchHostFederatedParams batchHostFederatedParams = (BatchHostFederatedParams) data.getBody();
        Model model = ((ServingServerContext) context).getModel();
        BatchInferenceResult batchInferenceResult = model.getModelProcessor().hostBatchInference(context, batchHostFederatedParams);
        Object requestInPostInterface = CustomInterfaceInstanceManager.getInstanceForName(MetaInfo.PROPERTY_INTERFACE_BATCH_HOST_POSTREQUEST);
        if(requestInPostInterface != null){
            CustomPreprocessHandle<BatchInferenceResult> requestInPostHandle = (CustomPreprocessHandle<BatchInferenceResult>)requestInPostInterface;
            requestInPostHandle.handle(context,batchInferenceResult);
        }
        return batchInferenceResult;
    }

    @Override
    protected OutboundPackage<BatchInferenceResult> serviceFailInner(Context context, InboundPackage<BatchInferenceRequest> data, Throwable e) {
        OutboundPackage<BatchInferenceResult> outboundPackage = new OutboundPackage<BatchInferenceResult>();
        BatchInferenceResult batchInferenceResult = new BatchInferenceResult();
        if (e instanceof BaseException) {
            BaseException baseException = (BaseException) e;
            batchInferenceResult.setRetcode(baseException.getRetcode());
            batchInferenceResult.setRetmsg(e.getMessage());
        } else {
            batchInferenceResult.setRetcode(StatusCode.SYSTEM_ERROR);
            batchInferenceResult.setRetmsg(e.getMessage());
        }
        context.setReturnCode(batchInferenceResult.getRetcode());
        outboundPackage.setData(batchInferenceResult);
        return outboundPackage;
    }

}
