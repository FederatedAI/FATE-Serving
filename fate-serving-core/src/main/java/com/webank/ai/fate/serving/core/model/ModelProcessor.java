package com.webank.ai.fate.serving.core.model;

import com.webank.ai.fate.serving.core.bean.BatchFederatedResult;
import com.webank.ai.fate.serving.core.bean.BatchHostFederatedParams;
import com.webank.ai.fate.serving.core.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.Context;

import java.util.concurrent.Future;

public interface ModelProcessor {

    public BatchFederatedResult  guestBatchInference(Context context, BatchInferenceRequest batchInferenceRequest, Future remoteFuture);

    public BatchFederatedResult  hostBatchInference(Context context, BatchHostFederatedParams batchHostFederatedParams);
}
