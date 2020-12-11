package com.webank.ai.fate.serving.core.rpc.sink;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;


public interface Sender<Req,Resp> {


    public Future<Resp> async(Context  context, Req req);
}
