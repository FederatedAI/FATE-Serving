package com.webank.ai.fate.serving.adaptor.dataaccess;

import com.webank.ai.fate.serving.core.adaptor.BatchFeatureDataAdaptor;
import com.webank.ai.fate.serving.core.bean.BatchHostFeatureAdaptorResult;
import com.webank.ai.fate.serving.core.bean.BatchHostFederatedParams;
import com.webank.ai.fate.serving.core.bean.Context;
import org.springframework.core.env.Environment;

import java.util.List;

public abstract class AbstractBatchFeatureDataAdaptor implements BatchFeatureDataAdaptor {

    protected Environment environment;

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public abstract void init();

    public abstract BatchHostFeatureAdaptorResult getFeatures(Context context, List<BatchHostFederatedParams.SingleInferenceData> featureIdList);

    @Override
    public List<ParamDescriptor> desc() {
        return null;
    }
}
