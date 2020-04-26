package com.webank.ai.fate.serving.admin.rpc.core;

import com.webank.ai.fate.serving.admin.bean.RouterInfo;
import lombok.Data;

import java.util.Map;

@Data
public class InboundPackage<T> {

    Map head;

    T body;

    String source;

    RouterInfo routerInfo;

}
