package com.webank.ai.fate.serving.host.interceptors;

import com.webank.ai.fate.register.utils.StringUtils;
import com.webank.ai.fate.serving.adapter.dataaccess.BatchFeatureDataAdaptor;
import com.webank.ai.fate.serving.common.interceptors.AbstractInterceptor;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.HostGetFeatureErrorException;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.utils.InferenceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class HostBatchFeatureAdaptorInterceptor extends AbstractInterceptor<BatchInferenceRequest, BatchInferenceResult> implements InitializingBean {

    Logger logger = LoggerFactory.getLogger(HostBatchFeatureAdaptorInterceptor.class);

    BatchFeatureDataAdaptor batchFeatureDataAdaptor = null;

    @Override
    public void doPreProcess(Context context, InboundPackage<BatchInferenceRequest> inboundPackage, OutboundPackage<BatchInferenceResult> outboundPackage) throws Exception {
        BatchInferenceRequest batchInferenceRequest = inboundPackage.getBody();
        if(batchFeatureDataAdaptor==null){

        }
        BatchHostFeatureAdaptorResult batchHostFeatureAdaptorResult = batchFeatureDataAdaptor.getFeatures(context, inboundPackage.getBody().getBatchDataList());
        if(batchHostFeatureAdaptorResult==null){
            throw  new HostGetFeatureErrorException("adaptor return null");
        }
        if(!StatusCode.SUCCESS.equals(batchHostFeatureAdaptorResult.getRetcode())){
            throw  new HostGetFeatureErrorException("adaptor return code is invalid {}",batchHostFeatureAdaptorResult.getRetcode());
        }
        Map<Integer, BatchHostFeatureAdaptorResult.SingleBatchHostFeatureAdaptorResult> featureResultMap = batchHostFeatureAdaptorResult.getIndexResultMap();
        batchInferenceRequest.getBatchDataList().forEach(request -> {
            request.setNeedCheckFeature(true);
            BatchHostFeatureAdaptorResult.SingleBatchHostFeatureAdaptorResult featureAdaptorResult = featureResultMap.get(request.getIndex());
            if (featureAdaptorResult!=null&&featureAdaptorResult.getFeatures() != null) {
                request.setFeatureData(featureAdaptorResult.getFeatures());
            }
        });
    };

    @Override
    public void afterPropertiesSet() throws Exception {
        String adaptorClass = environment.getProperty("feature.batch.adaptor");
        if (StringUtils.isNotEmpty(adaptorClass)) {
            batchFeatureDataAdaptor = (BatchFeatureDataAdaptor) InferenceUtils.getClassByName(adaptorClass);
            ServingServerContext context = new ServingServerContext();
            context.setEnvironment(environment);
            batchFeatureDataAdaptor.init(context);
        }
        logger.info("batch adaptor class is {}", adaptorClass);
    }

}
