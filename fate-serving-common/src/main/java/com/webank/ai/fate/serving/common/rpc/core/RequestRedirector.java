package com.webank.ai.fate.serving.common.rpc.core;

import com.webank.ai.fate.serving.core.bean.Context;

public interface RequestRedirector<Req,Resp> {

   Resp redirect(Context context, Req req,String serviceName);
}
