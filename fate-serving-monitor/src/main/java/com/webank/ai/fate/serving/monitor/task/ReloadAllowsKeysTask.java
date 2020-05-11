package com.webank.ai.fate.serving.monitor.task;

import com.alibaba.fastjson.JSONArray;
import com.webank.ai.fate.serving.monitor.utils.AllowKeysUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class ReloadAllowsKeysTask {

    private static final Logger logger = LoggerFactory.getLogger(ReloadAllowsKeysTask.class);
    private final String DEFAULT_CONFIG_FILE = "conf" + File.separator + "allowKeys.json";

    private String read(File file) {
        String result = "";
        try (
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
                BufferedReader reader = new BufferedReader(inputStreamReader)
        ) {
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                result += tempString;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Scheduled(fixedRate = 1000 * 60 * 10)
    public void reload() {
        String filePath = DEFAULT_CONFIG_FILE;
        logger.info("start refreshed allow keys...,try to load {}", filePath);

        File file = new File(filePath);
        if (!file.exists()) {
            logger.error("user keys config {} is not exist", filePath);
            return;
        }

        String data = read(file);

        JSONArray dataArray = JSONArray.parseArray(data);
        AllowKeysUtil.reload(dataArray);
    }


}
