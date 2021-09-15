/*
 * Copyright 2019 The FATE Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.ai.fate.serving.model;

import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.common.model.ModelProcessor;
import com.webank.ai.fate.serving.common.utils.TransferUtils;
import com.webank.ai.fate.serving.common.utils.ZipUtil;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.exceptions.ModelLoadException;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.federatedml.PipelineModelProcessor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Base64;
import java.util.List;
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
            modelProcessor.initModel(context, stringMap);
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
        Map<String, byte[]> data = unserialize(context, content);
        return data;
    }

    protected byte[] readFile(File file) {
        String outputPath = "";
        try {
            String tempDir = System.getProperty(Dict.PROPERTY_USER_HOME) + "/.fate/temp/";
            outputPath = ZipUtil.unzip(file, tempDir);
            File outputDir = new File(outputPath);
            if (!outputDir.exists()) {
                throw new FileNotFoundException();
            }
            Map<String, Object> resultMap = Maps.newHashMap();
            String root = outputDir.getAbsolutePath();
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
                            File dataFile = new File(root + File.separator + "variables" + File.separator + "data" + File.separator + keySplit[2] + File.separator + keySplit[3] + File.separator + keySplit[4]);
                            if (dataFile.exists()) {
                                in = new FileInputStream(dataFile);
                                Long length = dataFile.length();
                                byte[] content = new byte[length.intValue()];
                                int readCount = in.read(content);
                                if (readCount > 0) {
                                    String resultKey = keySplit[2] + "." + keySplit[3] + ":" + keySplit[4];
                                    resultMap.put(resultKey, content);
                                }
                                in.close();
                            } else {
                                logger.warn("model proto file not found {}", dataFile.getPath());
                            }
                        }
                    }

                } catch (Exception e) {
                    logger.error("read model file error, {}", e.getMessage());
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
            return JsonUtil.object2Json(resultMap).getBytes();
        } catch (Exception e) {
            logger.error("read file {} error, cause by {}", file.getAbsolutePath(), e.getMessage());
            e.printStackTrace();
        } finally {
            ZipUtil.clear(outputPath);
        }
        return null;
    }

    @Override
    public String getResource(Context context, ModelLoaderParam modelLoaderParam){
        return null;
    }
}
