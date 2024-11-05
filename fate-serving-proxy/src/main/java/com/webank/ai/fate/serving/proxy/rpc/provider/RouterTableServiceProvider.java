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

package com.webank.ai.fate.serving.proxy.rpc.provider;

import com.google.gson.JsonObject;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.serving.common.rpc.core.FateService;
import com.webank.ai.fate.serving.common.rpc.core.FateServiceMethod;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.RouterInfoOperateException;
import com.webank.ai.fate.serving.proxy.common.RouterTableUtils;
import com.webank.ai.fate.serving.proxy.rpc.grpc.RouterTableServiceProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@FateService(name = "routerTableService", preChain = {
        "requestOverloadBreaker"
}, postChain = {

})
@Service
public class RouterTableServiceProvider extends AbstractProxyServiceProvider {

    private static final Logger logger = LoggerFactory.getLogger(RouterTableServiceProvider.class);

    @FateServiceMethod(name = "QUERY_ROUTER")
    public RouterTableServiceProto.RouterOperatetResponse queryRouterTableService(Context context, InboundPackage inboundPackage) {
        RouterTableServiceProto.RouterOperatetResponse.Builder builder = RouterTableServiceProto.RouterOperatetResponse.newBuilder();
        JsonObject routTableJson = RouterTableUtils.loadRoutTable();
        if (routTableJson == null) {
            builder.setStatusCode(StatusCode.PROXY_LOAD_ROUTER_TABLE_ERROR);
            builder.setMessage("proxy load router table error");
            return builder.build();
        }

        builder.setStatusCode(StatusCode.SUCCESS);
        builder.setMessage(Dict.SUCCESS);
        ByteString bytes = ByteString.copyFrom(routTableJson.toString().getBytes());
        builder.setData(bytes);
        return builder.build();
    }

//    @FateServiceMethod(name = "ADD_ROUTER")
//    public RouterTableServiceProto.RouterOperatetResponse addRouterTableService(Context context, InboundPackage inboundPackage) {
//        RouterTableServiceProto.RouterOperatetResponse.Builder builder = RouterTableServiceProto.RouterOperatetResponse.newBuilder();
//        RouterTableServiceProto.RouterOperatetRequest request = (RouterTableServiceProto.RouterOperatetRequest) inboundPackage.getBody();
//        try {
//            RouterTableUtils.addRouter(request.getRouterTableInfoList());
//            builder.setStatusCode(StatusCode.SUCCESS);
//            builder.setMessage(Dict.SUCCESS);
//        } catch (RouterInfoOperateException e) {
//            builder.setStatusCode(StatusCode.PROXY_UPDATE_ROUTER_TABLE_ERROR);
//            builder.setMessage(e.getMessage());
//        }
//        return builder.build();
//    }

//    @FateServiceMethod(name = "UPDATE_ROUTER")
//    public RouterTableServiceProto.RouterOperatetResponse updateRouterTableService(Context context, InboundPackage inboundPackage) {
//        RouterTableServiceProto.RouterOperatetResponse.Builder builder = RouterTableServiceProto.RouterOperatetResponse.newBuilder();
//        RouterTableServiceProto.RouterOperatetRequest request = (RouterTableServiceProto.RouterOperatetRequest) inboundPackage.getBody();
//        try {
//            RouterTableUtils.updateRouter(request.getRouterTableInfoList());
//            builder.setStatusCode(StatusCode.SUCCESS);
//            builder.setMessage(Dict.SUCCESS);
//        } catch (RouterInfoOperateException e) {
//            builder.setStatusCode(StatusCode.PROXY_UPDATE_ROUTER_TABLE_ERROR);
//            builder.setMessage(e.getMessage());
//        }
//        return builder.build();
//    }
//
//    @FateServiceMethod(name = "DELETE_ROUTER")
//    public RouterTableServiceProto.RouterOperatetResponse deleteRouterTableService(Context context, InboundPackage inboundPackage) {
//        RouterTableServiceProto.RouterOperatetResponse.Builder builder = RouterTableServiceProto.RouterOperatetResponse.newBuilder();
//        RouterTableServiceProto.RouterOperatetRequest request = (RouterTableServiceProto.RouterOperatetRequest) inboundPackage.getBody();
//        try {
//            RouterTableUtils.deleteRouter(request.getRouterTableInfoList());
//            builder.setStatusCode(StatusCode.SUCCESS);
//            builder.setMessage(Dict.SUCCESS);
//        } catch (RouterInfoOperateException e) {
//            builder.setStatusCode(StatusCode.PROXY_UPDATE_ROUTER_TABLE_ERROR);
//            builder.setMessage(e.getMessage());
//        }
//        return builder.build();
//    }

    @FateServiceMethod(name = "SAVE_ROUTER")
    public RouterTableServiceProto.RouterOperatetResponse saveRouterTableService(Context context, InboundPackage inboundPackage) {
        RouterTableServiceProto.RouterOperatetResponse.Builder builder = RouterTableServiceProto.RouterOperatetResponse.newBuilder();
        RouterTableServiceProto.RouterOperatetRequest request = (RouterTableServiceProto.RouterOperatetRequest) inboundPackage.getBody();
        try {
            RouterTableUtils.saveRouter(request.getRouterInfo());
            builder.setStatusCode(StatusCode.SUCCESS);
            builder.setMessage(Dict.SUCCESS);
        } catch (RouterInfoOperateException e) {
            builder.setStatusCode(StatusCode.PROXY_UPDATE_ROUTER_TABLE_ERROR);
            builder.setMessage(e.getMessage());
        }
        return builder.build();
    }
}
