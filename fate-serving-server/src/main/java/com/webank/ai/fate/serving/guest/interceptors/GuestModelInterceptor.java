package com.webank.ai.fate.serving.guest.interceptors;

import com.google.common.base.Preconditions;
import com.webank.ai.fate.serving.common.bean.ServingServerContext;
import com.webank.ai.fate.serving.common.flow.FlowCounterManager;
import com.webank.ai.fate.serving.common.model.Model;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.Interceptor;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.exceptions.ModelNullException;
import com.webank.ai.fate.serving.model.ModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GuestModelInterceptor implements Interceptor {

    Logger logger = LoggerFactory.getLogger(GuestModelInterceptor.class);
    @Autowired
    ModelManager modelManager;
    @Autowired
    FlowCounterManager flowCounterManager;

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
        String serviceId = context.getServiceId();
        Model model = modelManager.getModelByServiceId(serviceId);
        Preconditions.checkArgument(model != null, "model is null");
        if (model == null) {
            throw new ModelNullException("can not find model by service id " + serviceId);
        }
        ((ServingServerContext) context).setModel(model);

        int times = 1;
        if (context.getServiceName().equalsIgnoreCase(Dict.SERVICENAME_BATCH_INFERENCE)) {
            BatchInferenceRequest batchInferenceRequest = (BatchInferenceRequest) inboundPackage.getBody();
            times = batchInferenceRequest.getBatchDataList().size();
        }
        context.putData(Dict.PASS_QPS, times);

        boolean pass = flowCounterManager.pass(model.getResourceName(), times);
        if (!pass) {
            flowCounterManager.block(model.getResourceName(), times);
        }
    }

}
