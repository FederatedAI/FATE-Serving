package com.webank.ai.fate.serving.proxy.security;

import com.google.common.base.Preconditions;
import com.webank.ai.fate.serving.proxy.rpc.core.Context;
import com.webank.ai.fate.serving.proxy.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.proxy.rpc.core.Interceptor;
import com.webank.ai.fate.serving.proxy.rpc.core.OutboundPackage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @Description TODO
 * @Author
 **/
@Service
public class FederationParamValidator   implements Interceptor {

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {

        Preconditions.checkArgument(StringUtils.isNotEmpty(context.getHostAppid()),"host id is null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(context.getGuestAppId()),"guest id is null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(context.getCaseId()),"case id is null");
    }

    @Override
    public void doPostProcess(Context context, InboundPackage inboundPackage,OutboundPackage outboundPackage) throws Exception {

    }
}
