package com.webank.ai.fate.serving.interceptor;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.webank.ai.fate.serving.core.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.Interceptor;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GuestBatchParamInterceptor     implements Interceptor {

    @Autowired
    Environment environment;

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {

        byte[]  reqBody = (byte[])inboundPackage.getBody();
        BatchInferenceRequest batchInferenceRequest =null;
        try {
             batchInferenceRequest = JSON.parseObject(reqBody, BatchInferenceRequest.class);
        }catch(Exception e){
            throw new  RuntimeException();
        }
        inboundPackage.setBody(batchInferenceRequest);
        Preconditions.checkArgument(batchInferenceRequest!=null,"");
        Preconditions.checkArgument(batchInferenceRequest.getDataList()!=null);
        Preconditions.checkArgument(StringUtils.isNotEmpty(batchInferenceRequest.getServiceId())&&
                StringUtils.isNotBlank(batchInferenceRequest.getServiceId()));
        context.setServiceId(batchInferenceRequest.getServiceId());

        List<BatchInferenceRequest.SingleInferenceData>  datalist = batchInferenceRequest.getDataList();

        int  batchSizeLimit = environment.getProperty("batch.inference.max", int.class, 50);

        Preconditions.checkArgument(datalist.size()<=batchSizeLimit);

//        for(BatchInferenceRequest.SingleInferenceData  singleInferenceData: datalist){
//            singleInferenceData.
//        }



    }


}
