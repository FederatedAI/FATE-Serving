package com.webank.ai.fate.serving.adapter.dataaccess;

import com.webank.ai.fate.register.utils.StringUtils;
import com.webank.ai.fate.serving.common.interceptors.AbstractInterceptor;
import com.webank.ai.fate.serving.core.bean.BatchHostFeatureAdaptorResult;
import com.webank.ai.fate.serving.core.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.BatchInferenceResult;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.utils.InferenceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BatchFeatureAdaptorInterceptor extends AbstractInterceptor<BatchInferenceRequest, BatchInferenceResult> implements InitializingBean {

    Logger logger = LoggerFactory.getLogger(BatchFeatureAdaptorInterceptor.class);

    BatchFeatureDataAdaptor batchFeatureDataAdaptor = null;

    @Override
    public void doPreProcess(Context context, InboundPackage<BatchInferenceRequest> inboundPackage, OutboundPackage<BatchInferenceResult> outboundPackage) throws Exception {
        BatchInferenceRequest batchInferenceRequest = inboundPackage.getBody();
        BatchHostFeatureAdaptorResult batchHostFeatureAdaptorResult = batchFeatureDataAdaptor.getFeatures(context, inboundPackage.getBody().getBatchDataList());
        Map<Integer, BatchHostFeatureAdaptorResult.SingleBatchHostFeatureAdaptorResult> featureResultMap = batchHostFeatureAdaptorResult.getIndexResultMap();
        batchInferenceRequest.getBatchDataList().forEach(request -> {
            BatchHostFeatureAdaptorResult.SingleBatchHostFeatureAdaptorResult featureAdaptorResult = featureResultMap.get(request.getIndex());
            if (featureAdaptorResult.getFeatures() != null) {
                request.setFeatureData(featureAdaptorResult.getFeatures());
            }
        });
        logger.info("after get features from local, batchInferenceRequest is {}", batchInferenceRequest);
    }

    ;

    @Override
    public void afterPropertiesSet() throws Exception {
        String adaptorClass = environment.getProperty("feature.batch.adaptor");
        if (StringUtils.isNotEmpty(adaptorClass)) {

            batchFeatureDataAdaptor = (BatchFeatureDataAdaptor) InferenceUtils.getClassByName(adaptorClass);
        }
        logger.info("batch adaptor class is {}", adaptorClass);

    }

}
