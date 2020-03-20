package com.webank.ai.fate.serving.guest.interceptors;

import com.google.common.base.Preconditions;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.exceptions.GuestModelNullException;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.Interceptor;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.model.NewModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GuestModelInterceptor implements Interceptor {

    Logger logger = LoggerFactory.getLogger(GuestModelInterceptor.class);

    @Autowired
    NewModelManager  modelManager;

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
        String serviceId = context.getServiceId();
        try {
            Model model = modelManager.getModelByServiceId(serviceId);
            Preconditions.checkArgument(model != null, "model is null");
            context.setModel(model);
        }catch (Exception  e){
            if(e instanceof IllegalArgumentException) {
                throw new GuestModelNullException("model is null");
            }
            else{
                throw  new  GuestModelNullException(e.getMessage());
            }

        }
    }
}
