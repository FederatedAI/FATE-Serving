/*
 * Copyright 2019 The FATE Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.ai.fate.serving.host.interceptors;

import com.webank.ai.fate.register.utils.StringUtils;
import com.webank.ai.fate.serving.adaptor.dataaccess.AbstractBatchFeatureDataAdaptor;
import com.webank.ai.fate.serving.common.interceptors.AbstractInterceptor;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.adaptor.BatchFeatureDataAdaptor;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.FeatureDataAdaptorException;
import com.webank.ai.fate.serving.core.exceptions.HostGetFeatureErrorException;
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
        long begin = System.currentTimeMillis();
        if (batchFeatureDataAdaptor == null) {
            throw new FeatureDataAdaptorException("adaptor not found");
        }
        BatchInferenceRequest batchInferenceRequest = inboundPackage.getBody();
        BatchHostFeatureAdaptorResult batchHostFeatureAdaptorResult = batchFeatureDataAdaptor.getFeatures(context, inboundPackage.getBody().getBatchDataList());
        if (batchHostFeatureAdaptorResult == null) {
            throw new HostGetFeatureErrorException("adaptor return null");
        }
        if (StatusCode.SUCCESS != batchHostFeatureAdaptorResult.getRetcode()) {
            throw new HostGetFeatureErrorException(batchHostFeatureAdaptorResult.getRetcode(), "adaptor return error");
        }
        Map<Integer, BatchHostFeatureAdaptorResult.SingleBatchHostFeatureAdaptorResult> featureResultMap = batchHostFeatureAdaptorResult.getIndexResultMap();
        batchInferenceRequest.getBatchDataList().forEach(request -> {
            request.setNeedCheckFeature(true);
            BatchHostFeatureAdaptorResult.SingleBatchHostFeatureAdaptorResult featureAdaptorResult = featureResultMap.get(request.getIndex());
            if (featureAdaptorResult != null && StatusCode.SUCCESS == featureAdaptorResult.getRetcode() && featureAdaptorResult.getFeatures() != null) {
                request.setFeatureData(featureAdaptorResult.getFeatures());
            }
        });
        long end = System.currentTimeMillis();
        logger.info("batch adaptor cost {} ", end - begin);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String adaptorClass = MetaInfo.PROPERTY_FEATURE_BATCH_ADAPTOR;
        if (StringUtils.isNotEmpty(adaptorClass)) {
            logger.info("try to load adaptor {}", adaptorClass);
            batchFeatureDataAdaptor = (BatchFeatureDataAdaptor) InferenceUtils.getClassByName(adaptorClass);
            try {
                ((AbstractBatchFeatureDataAdaptor) batchFeatureDataAdaptor).setEnvironment(environment);
                batchFeatureDataAdaptor.init();
            } catch (Exception e) {
                logger.error("batch adaptor init error");
            }
        }
        logger.info("batch adaptor class is {}", adaptorClass);
    }

}
