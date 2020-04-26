package com.webank.ai.fate.serving.admin.rpc.core;


import com.webank.ai.fate.serving.admin.bean.Context;

import java.util.List;

public interface ServiceAdaptor<req, rsp> {

    OutboundPackage<rsp> service(Context context, InboundPackage<req> inboundPackage) throws Exception;

    OutboundPackage<rsp> serviceFail(Context context, InboundPackage<req> data, List<Throwable> e) throws Exception;

}
