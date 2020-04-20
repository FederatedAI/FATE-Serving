package com.webank.ai.fate.serving.core.rpc.core;


import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.api.networking.proxy.*;
import com.webank.ai.fate.serving.core.model.Model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface FederatedRpcInvoker<T> {



    public  class RpcDataWraper  {
        Object  data;
        Model guestModel;
        Model   hostModel;
        String  remoteMethodName;


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
            if(hostModel!=null) {
                return hostModel;
            }else if(this.guestModel!=null){
                Map<String,Model> modelMap = this.getGuestModel().getFederationModelMap();
                List<String> keys = Lists.newArrayList(modelMap.keySet());
                return  modelMap.get(keys.get(0));
            }
            return  null;
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

    public T sync(Context context, RpcDataWraper rpcDataWraper,long  timeout );
    public  ListenableFuture<T> async(Context context, RpcDataWraper rpcDataWraper);


    }
