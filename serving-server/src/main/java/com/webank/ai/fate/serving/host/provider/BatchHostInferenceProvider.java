package com.webank.ai.fate.serving.host.provider;

import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.exceptions.BaseException;
import com.webank.ai.fate.serving.core.exceptions.ErrorCode;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.core.rpc.core.FateService;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.guest.provider.AbstractServingServiceProvider;
import com.webank.ai.fate.serving.model.NewModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
@FateService(name ="batchInferenece",  preChain= {
//        "overloadMonitor",
        "hostBatchParamInterceptor",
        "hostModelInterceptor",
      //  "federationRouterService"
},postChain = {
        "defaultPostProcess"
})
@Service
public class BatchHostInferenceProvider  extends AbstractServingServiceProvider<BatchInferenceRequest,BatchInferenceResult> {

    private static final Logger logger = LoggerFactory.getLogger(BatchHostInferenceProvider.class);

    @Override
    public BatchInferenceResult doService(Context context, InboundPackage data, OutboundPackage outboundPackage) {

        BatchHostFederatedParams  batchHostFederatedParams = (BatchHostFederatedParams)data.getBody();

        Model model =context.getModel();

        BatchInferenceResult batchInferenceResult = model.getModelProcessor().hostBatchInference(context,batchHostFederatedParams);

        return batchInferenceResult;
    }

    @Override
    protected  OutboundPackage<BatchInferenceResult>  serviceFailInner(Context context, InboundPackage<BatchInferenceRequest> data, Throwable e) throws Exception{

        OutboundPackage<BatchInferenceResult> outboundPackage = new OutboundPackage<BatchInferenceResult>();
        BatchInferenceResult  batchInferenceResult = new  BatchInferenceResult();
        if(e instanceof BaseException){
            BaseException  baseException = (BaseException) e;
            batchInferenceResult.setRetcode(baseException.getRetcode());
            batchInferenceResult.setMsg(e.getMessage());
        }else{
            batchInferenceResult.setRetcode(ErrorCode.SYSTEM_ERROR);
            batchInferenceResult.setMsg(e.getMessage());
        }
        outboundPackage.setData(batchInferenceResult);
        return  outboundPackage;
    }

}
