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

package com.webank.ai.fate.serving.federatedml.model;


import com.webank.ai.fate.core.mlmodel.buffer.OneHotMetaProto.OneHotMeta;
import com.webank.ai.fate.core.mlmodel.buffer.OneHotParamProto.ColsMap;
import com.webank.ai.fate.core.mlmodel.buffer.OneHotParamProto.OneHotParam;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.FederatedParams;
import com.webank.ai.fate.serving.core.bean.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class OneHotEncoder extends BaseModel {
    private static final Logger logger = LoggerFactory.getLogger(OneHotEncoder.class);

    private List<String> cols;
    private Map<String, ColsMap> colsMapMap;
    private boolean needRun;
    Pattern doublePattern = Pattern.compile("^-?([1-9]\\\\d*\\\\.\\\\d*|0\\\\.\\\\d*[1-9]\\\\d*|0?\\\\.0+|0)$");


    @Override
    public int initModel(byte[] protoMeta, byte[] protoParam) {
        logger.info("start init OneHot Encoder class");
        try {
            OneHotMeta oneHotMeta = this.parseModel(OneHotMeta.parser(), protoMeta);
            OneHotParam oneHotParam = this.parseModel(OneHotParam.parser(), protoParam);
            this.needRun = oneHotMeta.getNeedRun();

            this.cols = oneHotMeta.getTransformColNamesList();
            this.colsMapMap = oneHotParam.getColMapMap();
        } catch (Exception ex) {
            ex.printStackTrace();
             return StatusCode.ILLEGALDATA;
        }
        logger.info("Finish init OneHot Encoder class");
        return StatusCode.OK;
    }

    @Override
    public Map<String, Object> handlePredict(Context context, List<Map<String, Object>> inputData, FederatedParams predictParams) {
        logger.info("Start OneHot Encoder transform");
        logger.info("First time need run");
        HashMap<String, Object> outputData = new HashMap<>();
        Map<String, Object> firstData = inputData.get(0);
        logger.info("Need run is ", this.needRun);
        if (!this.needRun) {
            logger.info("Return firstData :", firstData);
            return firstData;
        }
        for (String colName : firstData.keySet()) {
            try{
                if (! this.cols.contains(colName)) {
                    outputData.put(colName, firstData.get(colName));
                    continue;
                }
                ColsMap colsMap = this.colsMapMap.get(colName);


                List<String> values = colsMap.getValuesList();
                List<String> encodedVariables = colsMap.getTransformedHeadersList();
//                Integer inputValue = new Integer(firstData.get(colName).toString());

                Integer inputValue = 0;
                try {
                    String thisInputValue = firstData.get(colName).toString();
                    if (this.isDouble(thisInputValue)) {
                        double d = Double.valueOf(thisInputValue);
                        inputValue = (int) Math.ceil(d);
                    }else {
                        inputValue = Integer.valueOf(thisInputValue);
                    }
                }catch (Throwable e){
                    logger.error("Onehot component accept number input value only");
                }

                for (int i = 0; i < values.size(); i ++) {
                    Integer possibleValue = Integer.parseInt(values.get(i));
                    String newColName = encodedVariables.get(i);
                    if (inputValue.equals(possibleValue)) {
                        outputData.put(newColName, 1.0);
                    }else {
                        outputData.put(newColName, 0.0);
                    }
                }
            }catch(Throwable e){
                logger.error("HeteroFeatureBinning error" ,e);
            }
        }
        logger.info("OneHotEncoder output {}",outputData);
        return outputData;
    }

    private boolean isDouble(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        return this.doublePattern.matcher(str).matches();
    }

}
