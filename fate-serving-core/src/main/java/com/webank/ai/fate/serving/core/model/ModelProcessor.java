package com.webank.ai.fate.serving.core.model;


import com.webank.ai.fate.serving.core.bean.*;

import java.util.concurrent.Future;

public interface ModelProcessor {

    public BatchInferenceResult guestBatchInference(Context context, BatchInferenceRequest batchInferenceRequest, Future remoteFuture,long timeout);

    public BatchInferenceResult  hostBatchInference(Context context, BatchHostFederatedParams batchHostFederatedParams);

    public BatchInferenceRequest guestPrepareDataBeforeInference(Context context, BatchInferenceRequest batchInferenceRequest);

    public ReturnResult guestInference(Context context, InferenceRequest inferenceRequest,Future remoteFuture,long timeout);

    public Object  getComponent(String  name);

}
