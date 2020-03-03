package com.webank.ai.fate.serving.interceptor;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.webank.ai.fate.serving.core.bean.BatchHostFederatedParams;
import com.webank.ai.fate.serving.core.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.Configuration;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.Interceptor;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HostBatchParamInterceptor implements Interceptor {

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
        List<BatchHostFederatedParams.SingleBatchHostFederatedParam>  datalist = batchHostFederatedParams.getDataList();
        int  batchSizeLimit = Configuration.getPropertyInt("batch.inference.max",50);
        Preconditions.checkArgument(datalist.size()<=batchSizeLimit);
//        for(BatchInferenceRequest.SingleInferenceData  singleInferenceData: datalist){
//            singleInferenceData.
//        }



    }


}
