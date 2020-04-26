package com.webank.ai.fate.serving.admin.rpc.core;

import com.webank.ai.fate.serving.admin.bean.Context;

public interface Interceptor<req, resp> {

    void doPreProcess(Context context, InboundPackage<req> inboundPackage, OutboundPackage<resp> outboundPackage) throws Exception;

    void doPostProcess(Context context, InboundPackage<req> inboundPackage, OutboundPackage<resp> outboundPackage) throws Exception;

}
