package com.webank.ai.fate.serving.core.rpc.core;

import com.webank.ai.fate.serving.core.bean.Context;

import java.util.List;

public interface ServiceAdaptor<req, rsp> {
    public OutboundPackage<rsp> service(Context context, InboundPackage<req> inboundPackage) throws Exception;

    public OutboundPackage<rsp> serviceFail( Context context,InboundPackage<req> data,List<Throwable> e) throws Exception;


}
