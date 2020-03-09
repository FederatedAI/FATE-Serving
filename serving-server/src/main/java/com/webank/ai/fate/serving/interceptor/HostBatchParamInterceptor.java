package com.webank.ai.fate.serving.interceptor;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.webank.ai.fate.serving.core.bean.BatchHostFederatedParams;
import com.webank.ai.fate.serving.core.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.Interceptor;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HostBatchParamInterceptor implements Interceptor {

    @Autowired
    private Environment environment;

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {

        byte[]  reqBody = (byte[])inboundPackage.getBody();
        BatchHostFederatedParams batchHostFederatedParams =null;
        try {
            batchHostFederatedParams = JSON.parseObject(reqBody, BatchInferenceRequest.class);
        }catch(Exception e){
            throw new  RuntimeException();
        }
        inboundPackage.setBody(batchHostFederatedParams);
        Preconditions.checkArgument(batchHostFederatedParams!=null,"");
        Preconditions.checkArgument(batchHostFederatedParams.getDataList()!=null);
        List<BatchHostFederatedParams.SingleInferenceData>  datalist = batchHostFederatedParams.getDataList();
        int  batchSizeLimit = environment.getProperty("batch.inference.max", int.class, 50);
        Preconditions.checkArgument(datalist.size()<=batchSizeLimit);
//        for(BatchInferenceRequest.SingleInferenceData  singleInferenceData: datalist){
//            singleInferenceData.
//        }



    }


}