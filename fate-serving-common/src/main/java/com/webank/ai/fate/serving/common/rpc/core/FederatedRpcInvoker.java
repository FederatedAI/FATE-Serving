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

package com.webank.ai.fate.serving.common.rpc.core;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.serving.common.model.Model;
import com.webank.ai.fate.serving.core.bean.BatchInferenceResult;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.ReturnResult;

import java.util.List;
import java.util.Map;

public interface FederatedRpcInvoker<T> {

    public ListenableFuture<BatchInferenceResult> batchInferenceRpcWithCache(Context context,
                                                                             RpcDataWraper rpcDataWraper, boolean useCache);
    public T sync(Context context, RpcDataWraper rpcDataWraper, long timeout);

    public ListenableFuture<T> async(Context context, RpcDataWraper rpcDataWraper);

    public ListenableFuture<ReturnResult> singleInferenceRpcWithCache(Context context,
                                                                      FederatedRpcInvoker.RpcDataWraper rpcDataWraper, boolean useCache);
    public class RpcDataWraper {
        Object data;
        Model guestModel;
        Model hostModel;
        String remoteMethodName;

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public Model getGuestModel() {
            return guestModel;
        }

        public void setGuestModel(Model guestModel) {
            this.guestModel = guestModel;
        }

        public Model getHostModel() {
            if (hostModel != null) {
                return hostModel;
            } else if (this.guestModel != null) {
                Map<String, Model> modelMap = this.getGuestModel().getFederationModelMap();
                List<String> keys = Lists.newArrayList(modelMap.keySet());
                return modelMap.get(keys.get(0));
            }
            return null;
        }

        public void setHostModel(Model hostModel) {
            this.hostModel = hostModel;
        }

        public String getRemoteMethodName() {
            return remoteMethodName;
        }

        public void setRemoteMethodName(String remoteMethodName) {
            this.remoteMethodName = remoteMethodName;
        }
    }

}
