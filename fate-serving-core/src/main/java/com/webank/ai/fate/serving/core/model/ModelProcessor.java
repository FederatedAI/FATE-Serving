package com.webank.ai.fate.serving.core.model;


import com.webank.ai.fate.serving.core.bean.*;

import java.util.Map;
import java.util.concurrent.Future;

public interface ModelProcessor {

    public BatchInferenceResult guestBatchInference(Context context, BatchInferenceRequest batchInferenceRequest, Map<String,Future> remoteFutureMap, long timeout);

    public BatchInferenceResult  hostBatchInference(Context context, BatchHostFederatedParams batchHostFederatedParams);

    public BatchInferenceRequest guestPrepareDataBeforeInference(Context context, BatchInferenceRequest batchInferenceRequest);

    public ReturnResult guestInference(Context context, InferenceRequest inferenceRequest,Map<String,Future> remoteFutureMap ,long timeout);

    public Object  getComponent(String  name);

}
