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

package com.webank.ai.fate.serving.common.provider;

import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.register.url.CollectionUtils;
import com.webank.ai.fate.serving.common.flow.FlowCounterManager;
import com.webank.ai.fate.serving.common.model.Model;
import com.webank.ai.fate.serving.common.rpc.core.FateService;
import com.webank.ai.fate.serving.common.rpc.core.FateServiceMethod;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.guest.provider.AbstractServingServiceProvider;
import com.webank.ai.fate.serving.model.ModelManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FateService(name = "modelService", preChain = {
        "requestOverloadBreaker"
}, postChain = {
})
@Service
public class ModelServiceProvider extends AbstractServingServiceProvider {
    @Autowired
    ModelManager modelManager;

    @Autowired
    FlowCounterManager flowCounterManager;

    @FateServiceMethod(name = "MODEL_LOAD")
    public Object load(Context context, InboundPackage data) {
        ModelServiceProto.PublishRequest publishRequest = (ModelServiceProto.PublishRequest) data.getBody();
        ReturnResult returnResult = modelManager.load(context, publishRequest);
        return returnResult;
    }

    @FateServiceMethod(name = "MODEL_PUBLISH_ONLINE")
    public Object bind(Context context, InboundPackage data) {
        ModelServiceProto.PublishRequest req = (ModelServiceProto.PublishRequest) data.getBody();
        ReturnResult returnResult = modelManager.bind(context, req);
        return returnResult;
    }

    @FateServiceMethod(name = "QUERY_MODEL")
    public ModelServiceProto.QueryModelResponse queryModel(Context context, InboundPackage data) {
        ModelServiceProto.QueryModelRequest req = (ModelServiceProto.QueryModelRequest) data.getBody();
        List<Model> models = modelManager.queryModel(context, req);
        ModelServiceProto.QueryModelResponse.Builder builder = ModelServiceProto.QueryModelResponse.newBuilder();
        if (CollectionUtils.isNotEmpty(models)) {
            for (int i = 0; i < models.size(); i++) {
                Model model = models.get(i);
                if (model == null) {
                    continue;
                }

                List<Map> rolePartyMapList = model.getRolePartyMapList();
                if (rolePartyMapList == null) {
                    rolePartyMapList = new ArrayList<>();
                }

                Map rolePartyMap = new HashMap();
                rolePartyMap.put(Dict.ROLE, model.getRole());
                rolePartyMap.put(Dict.PART_ID, model.getPartId());
                rolePartyMapList.add(rolePartyMap);

                if (model.getFederationModelMap() != null) {
                    for (Model value : model.getFederationModelMap().values()) {
                        rolePartyMap = new HashMap();
                        rolePartyMap.put(Dict.ROLE, value.getRole());
                        rolePartyMap.put(Dict.PART_ID, value.getPartId());
                        rolePartyMapList.add(rolePartyMap);
                    }
                }

                model.setRolePartyMapList(rolePartyMapList);
                model.setAllowQps(flowCounterManager.getAllowedQps(model.getResourceName()));

                ModelServiceProto.ModelInfoEx.Builder modelExBuilder = ModelServiceProto.ModelInfoEx.newBuilder();
                modelExBuilder.setIndex(i);
                modelExBuilder.setTableName(model.getTableName());
                modelExBuilder.setNamespace(model.getNamespace());
                if (model.getServiceIds() != null) {
                    modelExBuilder.addAllServiceIds(model.getServiceIds());
                }
                modelExBuilder.setContent(JsonUtil.object2Json(model));
                builder.addModelInfos(modelExBuilder.build());
            }
        }
        builder.setRetcode(StatusCode.SUCCESS);
        return builder.build();
    }

    @FateServiceMethod(name = "UNLOAD")
    public ModelServiceProto.UnloadResponse unload(Context context, InboundPackage data) {
        ModelServiceProto.UnloadRequest req = (ModelServiceProto.UnloadRequest) data.getBody();
        ModelServiceProto.UnloadResponse res = modelManager.unload(context, req);
        return res;
    }

    @FateServiceMethod(name = "UNBIND")
    public ModelServiceProto.UnbindResponse unbind(Context context, InboundPackage data) {
        ModelServiceProto.UnbindRequest req = (ModelServiceProto.UnbindRequest) data.getBody();
        ModelServiceProto.UnbindResponse res = modelManager.unbind(context, req);
        return res;
    }

    @Override
    protected Object transformExceptionInfo(Context context, ExceptionInfo data) {
        String actionType = context.getActionType();
        if (data != null) {
            int code = data.getCode();
            String msg = data.getMessage() != null ? data.getMessage().toString() : "";
            if (StringUtils.isNotEmpty(actionType)) {
                switch (actionType) {
                    case "MODEL_LOAD":
                        ;
                    case "MODEL_PUBLISH_ONLINE":
                        ReturnResult returnResult = new ReturnResult();
                        returnResult.setRetcode(code);
                        returnResult.setRetmsg(msg);
                        return returnResult;
                    case "QUERY_MODEL":
                        ModelServiceProto.QueryModelResponse queryModelResponse = ModelServiceProto.QueryModelResponse.newBuilder().setRetcode(code).setMessage(msg).build();
                        return queryModelResponse;
                    case "UNLOAD":
                        ModelServiceProto.UnloadResponse unloadResponse = ModelServiceProto.UnloadResponse.newBuilder().setStatusCode(code).setMessage(msg).build();
                        return unloadResponse;
                    case "UNBIND":
                        ModelServiceProto.UnbindResponse unbindResponse = ModelServiceProto.UnbindResponse.newBuilder().setStatusCode(code).setMessage(msg).build();
                        return unbindResponse;
                }

            }
        }
        return null;
    }
}
