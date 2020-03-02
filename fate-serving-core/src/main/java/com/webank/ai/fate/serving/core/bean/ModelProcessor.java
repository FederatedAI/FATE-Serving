package com.webank.ai.fate.serving.core.bean;

import java.util.concurrent.Future;

public interface ModelProcessor {



    public BatchFederatedResult  batchPredict(Context  context, BatchInferenceRequest batchFederatedParams, Future remoteFuture );
}
