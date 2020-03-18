package com.webank.ai.fate.serving.federatedml.model;


import java.util.Map;

public interface PrepareRemoteable {

    Map prepareRemoteData(Map data);
}
