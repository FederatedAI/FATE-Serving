package com.webank.ai.fate.serving.core.rpc.core;

import java.util.List;

public interface ServiceAdaptor<req, rsp> {
    public OutboundPackage<rsp> service(Context context, InboundPackage<req> inboundPackage) throws Exception;

    public OutboundPackage<rsp> serviceFail( Context context,InboundPackage<req> data,List<Throwable> e) throws Exception;


}
