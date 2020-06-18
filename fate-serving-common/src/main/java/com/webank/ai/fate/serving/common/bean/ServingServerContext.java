package com.webank.ai.fate.serving.common.bean;


import com.webank.ai.fate.serving.common.model.Model;
import com.webank.ai.fate.serving.core.bean.Dict;

public class ServingServerContext extends BaseContext {

    String tableName;

    String namespace;

    public Model getModel() {
        return (Model) this.dataMap.get(Dict.MODEL);
    }

    public void setModel(Model model) {
        this.dataMap.put(Dict.MODEL, model);
    }

    public String getModelTableName() {
        return tableName;
    }

    public void setModelTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getModelNamesapce() {
        return namespace;
    }

    public void setModelNamesapce(String modelNamesapce) {
        this.namespace = modelNamesapce;
    }

}
