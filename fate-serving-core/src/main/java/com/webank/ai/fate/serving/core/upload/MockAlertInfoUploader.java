package com.webank.ai.fate.serving.core.upload;


import com.webank.ai.fate.serving.core.async.AsyncMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockAlertInfoUploader  implements  AlertInfoUploader{

    Logger logger = LoggerFactory.getLogger(MockAlertInfoUploader.class);

    @Override
    public void upload(AsyncMessageEvent event) {
        logger.info("alert info {}",event);
    }
}
