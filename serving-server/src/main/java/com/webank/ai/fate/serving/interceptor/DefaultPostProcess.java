package com.webank.ai.fate.serving.interceptor;

import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import org.springframework.stereotype.Component;

@Component
public class DefaultPostProcess extends AbstractInterceptor {

    @Override
    public void doPostProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {

    }
}
