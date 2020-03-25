package com.webank.ai.fate.serving.core.bean;

import com.webank.ai.fate.serving.core.model.Model;

public class ServingServerContext extends BaseContext{

    String  tableName;

    String  namespace;

    public Model getModel() {
        return (Model)this.dataMap.get(Dict.MODEL);
    }


    public void setModel(Model model) {
        this.dataMap.put(Dict.MODEL,model);
    }

    public String  getModelTableName(){
        return  tableName;
    };

    public String  getModelNamesapce(){
        return  namespace;
    };

    public void  setModelTableName(String  tableName ){
        this.tableName =  tableName;
    };

    public void setModelNamesapce(String modelNamesapce){
        this.namespace =  modelNamesapce;
    };

}
