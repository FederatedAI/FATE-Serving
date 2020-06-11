package com.webank.ai.fate.serving.host.interceptors;


import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.ServingServerContext;
import com.webank.ai.fate.serving.core.exceptions.HostModelNullException;
import com.webank.ai.fate.serving.core.flow.FlowCounterManager;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.Interceptor;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.model.ModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HostModelInterceptor implements Interceptor {

    Logger logger = LoggerFactory.getLogger(HostModelInterceptor.class);

    @Autowired
    ModelManager modelManager;
    @Autowired
    FlowCounterManager flowCounterManager;

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {

        ServingServerContext servingServerContext = ((ServingServerContext) context);
        String tableName = servingServerContext.getModelTableName();
        String nameSpace = servingServerContext.getModelNamesapce();
        Model model = modelManager.getModelByTableNameAndNamespace(tableName, nameSpace);
        if (model == null) {
            logger.error("table name {} namepsace {} is not exist", tableName, nameSpace);
            throw new HostModelNullException("mode is null");
        }
        servingServerContext.setModel(model);

        boolean pass = flowCounterManager.pass(model.getResourceName());
        if (!pass) {
            flowCounterManager.block(model.getResourceName());
        }
    }

}
