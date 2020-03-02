package com.webank.ai.fate.serving.interceptor;

import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.Interceptor;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.manager.NewModelManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FederationModelInterceptor implements Interceptor {

    @Autowired
    NewModelManager  modelManager;

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {

        String  serviceId = context.getServiceId();
        Model model =modelManager.getModelByServiceId(serviceId);
        if(model==null){
            throw  new  RuntimeException();
        }
        context.setModel(model);
    }
}
