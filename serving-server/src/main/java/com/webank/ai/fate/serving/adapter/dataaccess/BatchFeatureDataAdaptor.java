package com.webank.ai.fate.serving.adapter.dataaccess;

import com.webank.ai.fate.serving.core.bean.BatchHostFeatureAdaptorResult;
import com.webank.ai.fate.serving.core.bean.BatchHostFederatedParams;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.ReturnResult;

import java.util.List;
import java.util.Map;

public interface BatchFeatureDataAdaptor  extends  AdaptorDescriptor{

    BatchHostFeatureAdaptorResult getFeatures(Context context , List<BatchHostFederatedParams.SingleInferenceData>  featureIdList);
}
