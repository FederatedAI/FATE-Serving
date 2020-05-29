package com.webank.ai.fate.serving.common.interceptors;


import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import org.springframework.stereotype.Service;

@Service
public class MonitorInterceptor extends AbstractInterceptor {

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
        context.preProcess();


    }
    ;

    @Override
    public void doPostProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
        context.postProcess(inboundPackage.getBody(), outboundPackage.getData());
    }
    ;

}
