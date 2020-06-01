package com.webank.ai.fate.serving.host.provider;

import com.google.common.base.Preconditions;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.model.ModelProcessor;
import com.webank.ai.fate.serving.core.rpc.core.*;
import com.webank.ai.fate.serving.federatedml.model.HeteroSecureBoostingTreeHost;
import com.webank.ai.fate.serving.guest.provider.AbstractServingServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@FateService(name = "HostInferenceProvider", preChain = {
        "monitorInterceptor",
//        "requestOverloadBreaker",
        "hostParamInterceptor",
        "hostModelInterceptor",
        "hostSingleFeatureAdaptorInterceptor"

}, postChain = {
        "monitorInterceptor"
})
@Service
public class HostSingleInferenceProvider extends AbstractServingServiceProvider<InferenceRequest, ReturnResult> {

    private static final Logger logger = LoggerFactory.getLogger(HostSingleInferenceProvider.class);

    @Override
    protected OutboundPackage<ReturnResult> serviceFailInner(Context context, InboundPackage<InferenceRequest> data, Throwable e) {
        OutboundPackage<ReturnResult> outboundPackage = new OutboundPackage<ReturnResult>();
        ReturnResult returnResult = ErrorMessageUtil.handleExceptionToReturnResult(e);
        outboundPackage.setData(returnResult);
        context.setReturnCode(returnResult.getRetcode());
        return outboundPackage;
    }


    @FateServiceMethod(name = "federatedInference")
    public ReturnResult federatedInference(Context context, InboundPackage<InferenceRequest> data) {

        InferenceRequest params = data.getBody();
        Map<String, Object> featureData = params.getFeatureData();
        Model model = ((ServingServerContext) context).getModel();
        ModelProcessor modelProcessor = model.getModelProcessor();
        ReturnResult result = modelProcessor.hostInference(context, params);
        return result;

    }

    @FateServiceMethod(name = "federatedInference4Tree")
    public ReturnResult federatedInference4Tree(Context context, InboundPackage<Map> data) {

        Map params = data.getBody();
        Model model = ((ServingServerContext) context).getModel();
        Object componentObject = model.getModelProcessor().getComponent(params.get(Dict.COMPONENT_NAME).toString());
        Preconditions.checkArgument(componentObject != null);
        HeteroSecureBoostingTreeHost heteroSecureBoostingTreeHost = (HeteroSecureBoostingTreeHost) componentObject;
        Map<String, Object> map = heteroSecureBoostingTreeHost.predictSingleRound(context, (Map<String, Object>) params.get(Dict.TREE_LOCATION));
        ReturnResult result = new ReturnResult();
        result.setRetcode(StatusCode.SUCCESS);
        result.setData(map);
        return result;
    }


}