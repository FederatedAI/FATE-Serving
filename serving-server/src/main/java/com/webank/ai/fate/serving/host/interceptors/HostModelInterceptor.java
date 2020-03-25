package com.webank.ai.fate.serving.host.interceptors;


import com.google.common.base.Preconditions;
import com.webank.ai.fate.serving.ServingServer;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.ServingServerContext;
import com.webank.ai.fate.serving.core.exceptions.GuestModelNullException;
import com.webank.ai.fate.serving.core.exceptions.HostModelNullException;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.Interceptor;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.model.NewModelManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HostModelInterceptor implements Interceptor {

    @Autowired
    NewModelManager   modelManager;

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
        try {

            ServingServerContext   servingServerContext =((ServingServerContext)context);
            Model model = modelManager.getModelByTableNameAndNamespace(servingServerContext.getModelTableName(),servingServerContext.getModelNamesapce());
            Preconditions.checkArgument(model != null);
            servingServerContext.setModel(model);
        }catch (Exception  e){
            if(e instanceof IllegalArgumentException) {
                throw new HostModelNullException("model is null");
            }
            else{
                throw  new HostModelNullException(e.getMessage());
            }
        }
    }

}
