package com.webank.ai.fate.serving.host;

import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;

import java.util.List;

public abstract  class AbstractHostProvider<req,resp>   extends AbstractServiceAdaptor<req,resp>{

    @Override
    public OutboundPackage<resp> serviceFail(Context context, InboundPackage<req> data, List<Throwable> errors) throws Exception {


        Throwable e = errors.get(0);
        return  serviceFailInner(context,data,e);

    }
}
