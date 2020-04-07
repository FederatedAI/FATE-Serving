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

package com.webank.ai.fate.serving.adapter.dataaccess;


import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.InferenceRetCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestFilePick implements SingleFeatureDataAdaptor {
    private static final Logger logger = LoggerFactory.getLogger(TestFilePick.class);
    private static final Map<String, Map<String, Object> > featureMaps = new HashMap<>();


    @Override
    public ReturnResult getData(Context context, Map<String, Object> featureIds) {
//        ReturnResult returnResult = new ReturnResult();
//
//        try {
//            if(featureMaps.isEmpty()){
//                List<String> lines = Files.readAllLines(Paths.get(System.getProperty(Dict.PROPERTY_USER_DIR), "host_data.csv"));
//                lines.forEach(line -> {
//                    String[] idFeats = StringUtils.split(line, ",");
//                    if(idFeats.length > 1){
//                        Map<String, Object> data = new HashMap<>();
//                        for (String kv : StringUtils.split(idFeats[1], ";")) {
//                            String[] a = StringUtils.split(kv, ":");
//                            data.put(a[0], Double.valueOf(a[1]));
//                        }
//                        featureMaps.put(idFeats[0], data);
//                    }
//                });
//            }
//            Map<String, Object> fdata = featureMaps.get(featureIds.get(Dict.DEVICE_ID));
//            if(fdata != null) {
//                returnResult.setData(fdata);
//                returnResult.setRetcode(InferenceRetCode.OK);
//            } else{
//                logger.error("cant not find features for {}.", featureIds.get(Dict.DEVICE_ID));
//                returnResult.setRetcode(InferenceRetCode.GET_FEATURE_FAILED);
//            }
//        } catch (Exception ex) {
//            logger.error(ex.getMessage());
//            returnResult.setRetcode(InferenceRetCode.GET_FEATURE_FAILED);
//        }
//        return returnResult;
        return null;
    }
}