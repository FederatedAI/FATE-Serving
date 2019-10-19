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

import com.webank.ai.fate.core.constant.StatusCode;
import com.webank.ai.fate.core.mlmodel.buffer.OneHotMetaProto.OneHotMeta;
import com.webank.ai.fate.core.mlmodel.buffer.OneHotParamProto.OneHotParam;
import com.webank.ai.fate.core.mlmodel.buffer.OneHotParamProto.ColsMap;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.FederatedParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OneHotEncoder extends BaseModel {
    private static final Logger LOGGER = LogManager.getLogger();

    private List<String> cols;
    private Map<String, ColsMap> colsMapMap;
    private boolean needRun;

    @Override
    public int initModel(byte[] protoMeta, byte[] protoParam) {
        LOGGER.info("start init OneHot Encoder class");
        try {
            OneHotMeta oneHotMeta = this.parseModel(OneHotMeta.parser(), protoMeta);
            OneHotParam oneHotParam = this.parseModel(OneHotParam.parser(), protoParam);
            this.needRun = oneHotMeta.getNeedRun();

            this.cols = oneHotMeta.getColsList();
            this.colsMapMap = oneHotParam.getColMapMap();
        } catch (Exception ex) {
            ex.printStackTrace();
             return StatusCode.ILLEGALDATA;
        }
        LOGGER.info("Finish init OneHot Encoder class");
        return StatusCode.OK;
    }

    @Override
    public Map<String, Object> handlePredict(Context context, List<Map<String, Object>> inputData, FederatedParams predictParams) {
        LOGGER.info("Start OneHot Encoder transform");
        HashMap<String, Object> outputData = new HashMap<>();
        Map<String, Object> firstData = inputData.get(0);
        if (!this.needRun) {
            return firstData;
        }
        for (String colName : firstData.keySet()) {
            try{
                if (! this.cols.contains(colName)) {
                    outputData.put(colName, firstData.get(colName));
                    continue;
                }
                ColsMap colsMap = this.colsMapMap.get(colName);

                String dataType = colsMap.getDataType();
                if (! dataType.equals("int")) {
                    LOGGER.warn("One Hot Encoder support integer input only now");
                    outputData.put(colName, firstData.get(colName));
                    continue;
                }
                List<String> values = colsMap.getValuesList();
                List<String> encodedVariables = colsMap.getEncodedVariablesList();
                Integer inputValue = new Integer(firstData.get(colName).toString());
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
                LOGGER.error("HeteroFeatureBinning error" ,e);
            }
        }
        return outputData;
    }
}
