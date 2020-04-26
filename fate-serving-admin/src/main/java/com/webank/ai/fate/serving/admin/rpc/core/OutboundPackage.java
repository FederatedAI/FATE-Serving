package com.webank.ai.fate.serving.admin.rpc.core;

import lombok.Data;

@Data
public class OutboundPackage<T> {

    T  data ;

}
