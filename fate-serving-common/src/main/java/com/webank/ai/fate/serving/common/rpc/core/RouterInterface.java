package com.webank.ai.fate.serving.common.rpc.core;


import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;


public interface RouterInterface extends Interceptor {
    RouterInfo route(Context context, InboundPackage inboundPackage);
}
