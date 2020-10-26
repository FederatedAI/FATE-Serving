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

import com.webank.ai.fate.serving.common.bean.ServingServerContext;
import com.webank.ai.fate.serving.common.flow.FlowCounterManager;
import com.webank.ai.fate.serving.common.model.Model;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.Interceptor;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.BatchHostFederatedParams;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.exceptions.HostModelNullException;
import com.webank.ai.fate.serving.model.ModelManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HostModelInterceptor implements Interceptor {

    Logger logger = LoggerFactory.getLogger(HostModelInterceptor.class);

    @Autowired
    ModelManager modelManager;
    @Autowired
    FlowCounterManager flowCounterManager;

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
        ServingServerContext servingServerContext = ((ServingServerContext) context);
        String tableName = servingServerContext.getModelTableName();
        String nameSpace = servingServerContext.getModelNamesapce();
        Model model;
        if (StringUtils.isBlank(context.getVersion()) || Double.parseDouble(context.getVersion()) < 200) {
            model = modelManager.getPartnerModel(tableName, nameSpace);
        } else {
            model = modelManager.getModelByTableNameAndNamespace(tableName, nameSpace);
        }
        if (model == null) {
            logger.error("table name {} namepsace {} is not exist", tableName, nameSpace);
            throw new HostModelNullException("mode is null");
        }
        servingServerContext.setModel(model);

        int times = 1;
        if (context.getServiceName().equalsIgnoreCase(Dict.SERVICENAME_BATCH_INFERENCE)) {
            BatchHostFederatedParams batchHostFederatedParams = (BatchHostFederatedParams) inboundPackage.getBody();
            times = batchHostFederatedParams.getBatchDataList().size();
        }
        context.putData(Dict.PASS_QPS, times);

        boolean pass = flowCounterManager.pass(model.getResourceName(), times);
        if (!pass) {
            flowCounterManager.block(model.getResourceName(), times);
        }
    }

}
