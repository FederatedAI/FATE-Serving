package com.webank.ai.fate.serving.guest.interceptors;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.webank.ai.fate.serving.core.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.exceptions.GuestInvalidParamException;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.Interceptor;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 */
@Service
public class GuestBatchParamInterceptor     implements Interceptor {

    Logger logger = LoggerFactory.getLogger(GuestBatchParamInterceptor.class);

    @Autowired
    Environment environment;
    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {

        byte[]  reqBody = (byte[])inboundPackage.getBody();
        BatchInferenceRequest batchInferenceRequest =null;

        try {
            batchInferenceRequest = JSON.parseObject(reqBody, BatchInferenceRequest.class);
            inboundPackage.setBody(batchInferenceRequest);
            Preconditions.checkArgument(batchInferenceRequest != null, "batch inference request parse error");
            Preconditions.checkArgument(batchInferenceRequest.getDataList() != null, "no inference data");
            Preconditions.checkArgument(StringUtils.isNotEmpty(batchInferenceRequest.getServiceId()) &&
                    StringUtils.isNotBlank(batchInferenceRequest.getServiceId()), "no service id");
            context.setServiceId(batchInferenceRequest.getServiceId());
            List<BatchInferenceRequest.SingleInferenceData> datalist = batchInferenceRequest.getDataList();
            int batchSizeLimit = environment.getProperty("batch.inference.max", int.class, 50);
            Preconditions.checkArgument(datalist.size() <= batchSizeLimit, "max batch inference data size cannot be greater than " + batchSizeLimit);
        }catch(Exception e){
            logger.error("invalid param",e);
            throw  new GuestInvalidParamException(e.getMessage());
        }
    }


}
