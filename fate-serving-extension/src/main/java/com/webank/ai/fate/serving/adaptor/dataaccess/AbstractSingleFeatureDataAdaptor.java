package com.webank.ai.fate.serving.adaptor.dataaccess;

import com.webank.ai.fate.serving.core.adaptor.SingleFeatureDataAdaptor;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import org.springframework.core.env.Environment;

import java.util.Map;

public abstract class AbstractSingleFeatureDataAdaptor implements SingleFeatureDataAdaptor {

    protected Environment environment;

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public abstract void init();

    public abstract ReturnResult getData(Context context, Map<String, Object> featureIds);

}
