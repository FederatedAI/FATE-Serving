package com.webank.ai.fate.serving.core.model;


import com.webank.ai.fate.serving.core.bean.BatchHostFederatedParams;
import com.webank.ai.fate.serving.core.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.BatchInferenceResult;
import com.webank.ai.fate.serving.core.bean.Context;

import java.util.concurrent.Future;

public interface ModelProcessor {

    public BatchInferenceResult guestBatchInference(Context context, BatchInferenceRequest batchInferenceRequest, Future remoteFuture);

    public BatchInferenceResult  hostBatchInference(Context context, BatchHostFederatedParams batchHostFederatedParams);

    public BatchInferenceRequest guestPrepareDataBeforeInference(Context context, BatchInferenceRequest batchInferenceRequest);
}
