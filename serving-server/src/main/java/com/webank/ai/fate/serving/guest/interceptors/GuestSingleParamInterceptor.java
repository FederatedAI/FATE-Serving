package com.webank.ai.fate.serving.guest.interceptors;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.serving.core.bean.InferenceRequest;
import com.webank.ai.fate.serving.core.utils.InferenceUtils;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.exceptions.GuestInvalidParamException;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.Interceptor;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class GuestSingleParamInterceptor implements Interceptor {

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {

        try {
            byte[] reqBody = (byte[]) inboundPackage.getBody();
            InferenceServiceProto.InferenceMessage   message=   InferenceServiceProto.InferenceMessage.parseFrom(reqBody);
            InferenceRequest inferenceRequest = null;
            inferenceRequest = JSON.parseObject(       message.getBody().toByteArray(), InferenceRequest.class);
            inboundPackage.setBody(inferenceRequest);
            Preconditions.checkArgument(inferenceRequest != null, "request message parse error");
            Preconditions.checkArgument(inferenceRequest.getFeatureData() != null, "no feature data");
            Preconditions.checkArgument(inferenceRequest.getSendToRemoteFeatureData() != null, "no send to remote feature data");
            Preconditions.checkArgument(StringUtils.isNotBlank(inferenceRequest.getServiceId()), "no service id");
            if (inferenceRequest.getCaseid().length() == 0) {
                inferenceRequest.setCaseId(InferenceUtils.generateCaseid());
            }
            context.setCaseId(inferenceRequest.getCaseid());
            context.setServiceId(inferenceRequest.getServiceId());
        }catch(Exception e){
            throw  new GuestInvalidParamException(e.getMessage());
        }

    }


}
