package com.webank.ai.fate.serving.model;

import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.exceptions.ModelLoadException;
import com.webank.ai.fate.serving.core.model.ModelProcessor;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.core.utils.TransferUtils;
import com.webank.ai.fate.serving.federatedml.PipelineModelProcessor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocalPbModelLoader extends AbstractModelLoader<Map<String, byte[]>> {
    @Override
    protected byte[] serialize(Context context, Map<String, byte[]> data) {
        Map<String, String> result = Maps.newHashMap();
        if (data != null) {
            data.forEach((k, v) -> {
                String base64String = new String(Base64.getEncoder().encode(v));
                result.put(k, base64String);
            });
            return JsonUtil.object2Json(result).getBytes();
        }
        return null;
    }

    @Override
    protected Map<String, byte[]> unserialize(Context context, byte[] data) {
        Map<String, byte[]> result = Maps.newHashMap();
        try {
            if (data != null) {
                Map originData = JsonUtil.json2Object(data, Map.class);
                if (originData != null) {
                    originData.forEach((k, v) -> {
                        result.put(k.toString(), Base64.getDecoder().decode(v.toString()));
                    });
                    return result;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        if (file != null && file.exists() && file.isDirectory()) {
            Map<String, Object> resultMap = new HashMap<>();
            String root = file.getAbsolutePath();
            List<String> properties = TransferUtils.yml2Properties(root + File.separator + "define" + File.separator + "define_meta.yaml");
            if (properties != null) {
                InputStream in = null;
                try {
                    for (String property : properties) {
                        if (property.startsWith("describe.model_proto")) {
                            String[] split = property.split("=");
                            String key = split[0];
                            String value = split[1];

                            String[] keySplit = key.split("\\.");
                            File dataFile = new File(root + File.separator + "variables" + File.separator + "data" + File.separator + keySplit[2] + File.separator + keySplit[3] + File.separator + value);
                            if (dataFile.exists()) {
                                in = new FileInputStream(dataFile);
                                Long length = dataFile.length();
                                byte[] content = new byte[length.intValue()];
                                int readCount = in.read(content);
                                if (readCount > 0) {
                                    String resultKey = keySplit[2] + "." + keySplit[3] + ":" + keySplit[4];
                                    resultMap.put(resultKey, content);
                                }
                            } else {
                                logger.warn("model proto file not found {}", dataFile.getPath());
                            }
                        }
                    }
                    return JsonUtil.object2Json(resultMap).getBytes();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return null;
    }
}
