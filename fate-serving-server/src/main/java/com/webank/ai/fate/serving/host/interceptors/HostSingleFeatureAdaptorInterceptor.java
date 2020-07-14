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
import com.webank.ai.fate.serving.adaptor.dataaccess.AbstractSingleFeatureDataAdaptor;
import com.webank.ai.fate.serving.common.interceptors.AbstractInterceptor;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.adaptor.SingleFeatureDataAdaptor;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.InferenceRequest;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.FeatureDataAdaptorException;
import com.webank.ai.fate.serving.core.exceptions.HostGetFeatureErrorException;
import com.webank.ai.fate.serving.core.utils.InferenceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

@Service
public class HostSingleFeatureAdaptorInterceptor extends AbstractInterceptor<InferenceRequest, ReturnResult> implements InitializingBean {
    Logger logger = LoggerFactory.getLogger(HostSingleFeatureAdaptorInterceptor.class);
    SingleFeatureDataAdaptor singleFeatureDataAdaptor = null;

    @Override
    public void doPreProcess(Context context, InboundPackage<InferenceRequest> inboundPackage, OutboundPackage<ReturnResult> outboundPackage) throws Exception {
        if (singleFeatureDataAdaptor == null) {
            throw new FeatureDataAdaptorException("adaptor not found");
        }
        InferenceRequest inferenceRequest = inboundPackage.getBody();
        ReturnResult singleFeatureDataAdaptorData = singleFeatureDataAdaptor.getData(context, inboundPackage.getBody().getSendToRemoteFeatureData());
        if (singleFeatureDataAdaptorData == null) {
            throw new HostGetFeatureErrorException("adaptor return null");
        }
        if (!StatusCode.SUCCESS.equals(singleFeatureDataAdaptorData.getRetcode())) {
            throw new HostGetFeatureErrorException(singleFeatureDataAdaptorData.getRetcode(), "adaptor return error");
        }
        inferenceRequest.getFeatureData().putAll(singleFeatureDataAdaptorData.getData());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String adaptorClass = MetaInfo.PROPERTY_FEATURE_SINGLE_ADAPTOR;
        if (StringUtils.isNotEmpty(adaptorClass)) {
            logger.info("try to load adaptor {}", adaptorClass);
            singleFeatureDataAdaptor = (SingleFeatureDataAdaptor) InferenceUtils.getClassByName(adaptorClass);
            ((AbstractSingleFeatureDataAdaptor) singleFeatureDataAdaptor).setEnvironment(environment);
            try {
                singleFeatureDataAdaptor.init();
            } catch (Exception e) {
                logger.error("single adaptor init error");
            }
            logger.info("single adaptor class is {}", adaptorClass);
        }
    }

}



