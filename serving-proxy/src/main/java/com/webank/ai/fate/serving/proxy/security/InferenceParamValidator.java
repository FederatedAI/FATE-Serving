package com.webank.ai.fate.serving.proxy.security;

import com.google.common.base.Preconditions;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.Interceptor;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.proxy.common.Dict;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @Description TODO
 * @Author
 **/
@Service
public class InferenceParamValidator implements Interceptor{

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotEmpty(context.getCaseId()),"case id is null");
        Preconditions.checkArgument(null != inboundPackage.getHead(),Dict.HEAD + " is null");
        Preconditions.checkArgument(null != inboundPackage.getBody(),Dict.BODY + " is null");
        //Preconditions.checkArgument(StringUtils.isNotEmpty((String) inboundPackage.getHead().get(Dict.SERVICE_ID)), Dict.SERVICE_ID + " is null");
    }

    @Override
    public void doPostProcess(Context context, InboundPackage inboundPackage,OutboundPackage outboundPackage) throws Exception {

    }
}
