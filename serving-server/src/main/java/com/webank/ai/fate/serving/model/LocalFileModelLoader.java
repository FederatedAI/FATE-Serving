package com.webank.ai.fate.serving.model;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.exceptions.ModelLoadException;
import com.webank.ai.fate.serving.core.model.ModelProcessor;
import com.webank.ai.fate.serving.federatedml.PipelineModelProcessor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;

@Service
public class LocalFileModelLoader extends AbstractModelLoader<Map<String, byte[]>> {
    @Override
    protected byte[] serialize(Context context, Map<String, byte[]> data) {
        Map<String, String> result = Maps.newHashMap();
        if (data != null) {
            data.forEach((k, v) -> {
                String base64String = new String(Base64.getEncoder().encode(v));
                result.put(k, base64String);
            });
            return JSON.toJSONString(result).getBytes();
        }
        return null;
    }

    @Override
    protected Map<String, byte[]> unserialize(Context context, byte[] data) {
        Map<String, byte[]> result = Maps.newHashMap();
        if (data != null) {
            String dataString = new String(data);
            Map originData = JSON.parseObject(dataString, Map.class);
            if (originData != null) {
                originData.forEach((k, v) -> {
                    result.put(k.toString(), Base64.getDecoder().decode(v.toString()));
                });
                return result;
            }
        }
        return null;
    }

    @Override
    protected ModelProcessor initPipeLine(Context context, Map<String, byte[]> stringMap) {
        if (stringMap != null) {
            PipelineModelProcessor modelProcessor = new PipelineModelProcessor();
            modelProcessor.initModel(stringMap);
            return modelProcessor;
        } else {
            return null;
        }
    }

    @Override
    protected Map<String, byte[]> doLoadModel(Context context, ModelLoaderParam modelLoaderParam) {

        String filePath = modelLoaderParam.getFilePath();
        File file = new File(filePath);
        if (!file.exists()) {
            throw new ModelLoadException("model file" + file.getAbsolutePath() + " is not exist");
        }
        byte[] content = readFile(file);
        // TODO: 2020/4/5 暂时拿原来的缓存文件来用
        Map<String, byte[]> data = unserialize(context, content);

        return data;
    }

    protected byte[] readFile(File file) {
        if (file != null && file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                Long filelength = file.length();
                byte[] filecontent = new byte[filelength.intValue()];
                int readCount = in.read(filecontent);
                if (readCount > 0) {
                    return filecontent;
                } else {
                    return null;
                }
            } catch (Throwable e) {
                logger.error("failed to doRestore file ", e);
            }
        }
        return null;
    }
}
