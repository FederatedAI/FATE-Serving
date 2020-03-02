package com.webank.ai.fate.serving.interceptor;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.webank.ai.fate.serving.core.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.Interceptor;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class BatchParamInterceptor     implements Interceptor {

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {

        String  body = (String)inboundPackage.getBody();
        BatchInferenceRequest batchInferenceRequest =null;
        try {
             batchInferenceRequest = JSON.parseObject(body, BatchInferenceRequest.class);
        }catch(Exception e){
            throw new  RuntimeException();
        }
        Preconditions.checkArgument(batchInferenceRequest!=null);
        Preconditions.checkArgument(batchInferenceRequest.getDataList()!=null);
        Preconditions.checkArgument(StringUtils.isNotEmpty(batchInferenceRequest.getServiceId())&&
                StringUtils.isNotBlank(batchInferenceRequest.getServiceId()));

        context.setServiceId(batchInferenceRequest.getServiceId());

    }


}
