package com.webank.ai.fate.serving.common.async;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockAlertInfoUploader implements AlertInfoUploader {

    Logger logger = LoggerFactory.getLogger(MockAlertInfoUploader.class);

    @Override
    public void upload(AsyncMessageEvent event) {
        logger.warn("alert info {}",event);
    }
}
