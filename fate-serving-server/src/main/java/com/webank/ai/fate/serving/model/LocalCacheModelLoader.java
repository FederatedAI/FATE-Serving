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
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.exceptions.ModelLoadException;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.federatedml.PipelineModelProcessor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Map;

@Service
public class LocalCacheModelLoader extends AbstractModelLoader<Map<String, byte[]>> {
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
        if (data != null) {
            String dataString = null;
            try {
                dataString = new String(data, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.getMessage();
            }
            Map originData = JsonUtil.json2Object(dataString, Map.class);
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
            throw new ModelLoadException("model cache file " + file.getAbsolutePath() + " is not exist");
        }
        byte[] content = readFile(file);
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

    @Override
    public String getResource(Context context, ModelLoaderParam modelLoaderParam) {
        return null;
    }
}
