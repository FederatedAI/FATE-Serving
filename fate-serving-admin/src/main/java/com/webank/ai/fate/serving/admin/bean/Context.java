package com.webank.ai.fate.serving.admin.bean;

import lombok.Data;

@Data
public class Context {

    private String version;

    private String callName;

    private String serviceName;

    private String host;

    private Integer port;

}
