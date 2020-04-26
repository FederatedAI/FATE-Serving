package com.webank.ai.fate.serving.guest.interceptors;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.serving.core.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.exceptions.GuestInvalidParamException;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.Interceptor;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.utils.InferenceUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class GuestBatchParamInterceptor implements Interceptor {

    Logger logger = LoggerFactory.getLogger(GuestBatchParamInterceptor.class);

    @Autowired
    Environment environment;

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {

        byte[] reqBody = (byte[]) inboundPackage.getBody();
        BatchInferenceRequest batchInferenceRequest = null;

        try {

            InferenceServiceProto.InferenceMessage message = InferenceServiceProto.InferenceMessage.parseFrom(reqBody);

            batchInferenceRequest = JSON.parseObject(message.getBody().toByteArray(), BatchInferenceRequest.class);
            logger.info("batch inference request {}", batchInferenceRequest);
            inboundPackage.setBody(batchInferenceRequest);
            Preconditions.checkArgument(batchInferenceRequest != null, "request message parse error");
//            Preconditions.checkArgument(inferenceRequest.getFeatureData() != null, "no feature data");
//            Preconditions.checkArgument(inferenceRequest.getSendToRemoteFeatureData() != null, "no send to remote feature data");
            Preconditions.checkArgument(StringUtils.isNotBlank(batchInferenceRequest.getServiceId()), "no service id");
            if (batchInferenceRequest.getCaseid().length() == 0) {
                batchInferenceRequest.setCaseId(InferenceUtils.generateCaseid());
            }
            context.setCaseId(batchInferenceRequest.getCaseid());
            context.setServiceId(batchInferenceRequest.getServiceId());
        } catch (Exception e) {
            throw new GuestInvalidParamException(e.getMessage());
        }


    }


}
