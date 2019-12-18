package com.webank.ai.fate.serving.proxy.rpc.router;


import com.webank.ai.fate.serving.proxy.rpc.core.Context;
import com.webank.ai.fate.serving.proxy.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.proxy.rpc.core.Interceptor;

public interface RouterInterface extends Interceptor{
    RouterInfo route(Context context, InboundPackage   inboundPackage);
}
