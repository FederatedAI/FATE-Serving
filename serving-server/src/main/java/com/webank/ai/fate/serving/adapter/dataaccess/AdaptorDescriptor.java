package com.webank.ai.fate.serving.adapter.dataaccess;

import java.util.List;

public interface AdaptorDescriptor {

    public static  class  ParamDescriptor{
        String  keyName;
        String  keyType;
        boolean isRequired;
    }

    public List<ParamDescriptor>  desc();
}
