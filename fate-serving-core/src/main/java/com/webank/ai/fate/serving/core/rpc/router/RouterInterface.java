package com.webank.ai.fate.serving.core.rpc.router;


import com.webank.ai.fate.serving.core.rpc.core.Context;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.Interceptor;

public interface RouterInterface extends Interceptor {
    RouterInfo route(Context context, InboundPackage inboundPackage);
}
