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

package com.webank.ai.fate.serving.proxy.rpc.services;

import com.webank.ai.fate.serving.common.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.common.rpc.core.FateService;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@FateService(name = "NotFound")
public class NotFoundService extends AbstractServiceAdaptor<String, Map> {
    @Override
    public Map doService(Context context, InboundPackage<String> data, OutboundPackage<Map> outboundPackage) {
        ReturnResult returnResult = ReturnResult.build(StatusCode.SERVICE_NOT_FOUND, "SERVICE_NOT_FOUND");
        return JsonUtil.json2Object(returnResult.toString(), Map.class);
    }

    @Override
    public OutboundPackage<Map> serviceFail(Context context, InboundPackage<String> data, List<Throwable> e) {
        return null;
    }

    @Override
    protected Map transformExceptionInfo(Context context, ExceptionInfo exceptionInfo) {
        return null;
    }
}
