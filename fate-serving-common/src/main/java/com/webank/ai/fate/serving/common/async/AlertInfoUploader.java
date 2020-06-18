package com.webank.ai.fate.serving.common.async;


public interface AlertInfoUploader {

    public void upload(AsyncMessageEvent event);
}
