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

package com.webank.ai.fate.serving.adaptor.dataaccess;

import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.FeatureDataAdaptorException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestFilePickAdapter extends AbstractSingleFeatureDataAdaptor {
    private static final Logger logger = LoggerFactory.getLogger(TestFilePickAdapter.class);
    private static final Map<String, Map<String, Object>> featureMaps = new HashMap<>();

    @Override
    public void init() {

    }

    @Override
    public ReturnResult getData(Context context, Map<String, Object> featureIds) {
        ReturnResult returnResult = new ReturnResult();

        try {
            if (featureMaps.isEmpty()) {
                logger.info("testHost data path = {}", Paths.get(System.getProperty(Dict.PROPERTY_USER_DIR), "host_data.csv"));
                List<String> lines = Files.readAllLines(Paths.get(System.getProperty(Dict.PROPERTY_USER_DIR), "host_data.csv"));
                for (int i = 0; i < lines.size(); i++) {
                    String[] idFeats = StringUtils.split(lines.get(i), ",");
                    if(idFeats.length == 2){
                        Map<String, Object> data = new HashMap<>();
                        for (String kv : StringUtils.split(idFeats[1], ";")) {
                            String[] a = StringUtils.split(kv, ":");
                            data.put(a[0], Double.valueOf(a[1]));
                        }
                        featureMaps.put(idFeats[0], data);
                    } else {
                        logger.error("please check the format for line " + (i + 1));
                        featureMaps.clear();
                        throw new FeatureDataAdaptorException("please check the host_data.csv format for line " + (i + 1));
                    }
                }
            }

            Map<String, Object> featureData = featureMaps.get(featureIds.get(Dict.ID).toString());
            if (featureData != null) {
                Map clone = (Map) ((HashMap) featureData).clone();
                returnResult.setData(clone);
                returnResult.setRetcode(StatusCode.SUCCESS);
            } else {
                logger.error("cant not find features for {}.", featureIds.get(Dict.ID).toString());
                returnResult.setRetcode(StatusCode.HOST_FEATURE_NOT_EXIST);
                returnResult.setRetmsg("cant not find features for " +  featureIds.get(Dict.ID).toString());
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            returnResult.setRetcode(StatusCode.HOST_FEATURE_ERROR);
        }
        return returnResult;
    }

}