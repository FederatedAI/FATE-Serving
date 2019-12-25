package com.webank.ai.fate.serving.proxy.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.DigestUtils;

public class FileUtils {
    private static final Logger LOGGER = LogManager.getLogger(FileUtils.class);

    public static String fileMd5(String filePath) {
        InputStream in = null;
        try {
            in = new FileInputStream(filePath);
            return DigestUtils.md5DigestAsHex(in);
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            }
        }
        return null;
    }



}
