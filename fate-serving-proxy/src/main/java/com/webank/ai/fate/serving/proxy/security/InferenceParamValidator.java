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

package com.webank.ai.fate.serving.proxy.security;

import com.google.common.base.Preconditions;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.Interceptor;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.exceptions.GuestInvalidParamException;
import com.webank.ai.fate.serving.core.exceptions.UnSupportMethodException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @Description TODO
 * @Author
 **/
@Service
public class InferenceParamValidator implements Interceptor {

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {

        try {
            Preconditions.checkArgument(StringUtils.isNotEmpty(context.getCaseId()), "case id is null");
            Preconditions.checkArgument(null != inboundPackage.getHead(), Dict.HEAD + " is null");
            //Preconditions.checkArgument(null != inboundPackage.getBody(),Dict.BODY + " is null");
            //Preconditions.checkArgument(StringUtils.isNotEmpty((String) inboundPackage.getHead().get(Dict.SERVICE_ID)), Dict.SERVICE_ID + " is null");
            Preconditions.checkArgument(Dict.SERVICENAME_INFERENCE.equals(context.getCallName()),"invalid call name") ;
        }catch(Exception e){

            throw new GuestInvalidParamException(e.getMessage());
        }

    }

    @Override
    public void doPostProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {

    }
}
