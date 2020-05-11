package com.webank.ai.fate.serving.core.upload;

import com.webank.ai.fate.serving.core.async.AsyncMessageEvent;

public interface AlertInfoUploader {

    public void upload(AsyncMessageEvent event);
}
