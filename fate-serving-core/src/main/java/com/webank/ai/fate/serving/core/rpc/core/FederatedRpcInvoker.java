package com.webank.ai.fate.serving.core.rpc.core;


import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.api.networking.proxy.*;

public interface FederatedRpcInvoker<T> {

    public T sync(Context context, Object   data , String remoteMethodName,long  timeout );
    public  ListenableFuture<T> async(Context context, Object   data , String remoteMethodName);


    }
