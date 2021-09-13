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

import com.webank.ai.fate.core.mlmodel.buffer.DataTransformMetaProto.DataTransformMeta;
import com.webank.ai.fate.core.mlmodel.buffer.DataTransformParamProto.DataTransformParam;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataTransform extends BaseComponent {
    private static final Logger logger = LoggerFactory.getLogger(DataTransform.class);
    private DataTransformMeta dataTransformMeta;
    private DataTransformParam dataTransformParam;
    private List<String> header;
    private String inputformat;
    private Imputer imputer;
    private Outlier outlier;
    private boolean isImputer;
    private boolean isOutlier;

    @Override
    public int initModel(byte[] protoMeta, byte[] protoParam) {
        logger.info("start init DataTransform class");
        try {
            this.dataTransformMeta = this.parseModel(DataTransformMeta.parser(), protoMeta);
            this.dataTransformParam = this.parseModel(DataTransformParam.parser(), protoParam);
            this.isImputer = this.dataTransformMeta.getImputerMeta().getIsImputer();
            logger.info("data transform isImputer {}", this.isImputer);
            if (this.isImputer) {
                this.imputer = new Imputer(this.dataTransformMeta.getImputerMeta().getMissingValueList(),
                        this.dataTransformParam.getImputerParam().getMissingReplaceValue());
            }
            this.isOutlier = this.dataTransformMeta.getOutlierMeta().getIsOutlier();
            logger.info("data io isOutlier {}", this.isOutlier);
            if (this.isOutlier) {
                this.outlier = new Outlier(this.dataTransformMeta.getOutlierMeta().getOutlierValueList(),
                        this.dataTransformParam.getOutlierParam().getOutlierReplaceValue());
            }
            this.header = this.dataTransformParam.getHeaderList();
            this.inputformat = this.dataTransformMeta.getInputFormat();
        } catch (Exception ex) {
            //ex.printStackTrace();
            logger.error("init DataTransform error", ex);
            return ILLEGALDATA;
        }
        logger.info("Finish init DataTransform class");
        return OK;
    }

    @Override
    public Object getParam() {
        return  dataTransformParam;
    }

    @Override
    public Map<String, Object> localInference(Context context, List<Map<String, Object>> inputData) {
        Map<String, Object> data = inputData.get(0);
        Map<String, Object> outputData = new HashMap<>();
        if (this.inputformat.equals(Dict.TAG_INPUT_FORMAT) || this.inputformat.equals(Dict.SPARSE_INPUT_FORMAT
        )) {
            if (logger.isDebugEnabled()) {
                logger.debug("Sparse Data Filling");
            }
            for (String col : this.header) {
                if (this.isImputer) {
                    outputData.put(col, data.getOrDefault(col, ""));
                } else {
                    outputData.put(col, data.getOrDefault(col, 0));
                }
            }
            if(logger.isDebugEnabled()) {
                logger.debug("sparse input-format, data {}", outputData);
            }
        } else {
            outputData = data;
            if (logger.isDebugEnabled()) {
                logger.debug("Dense input-format, not filling, {}", outputData);
            }
        }
        if (this.isImputer) {
            outputData = this.imputer.transform(outputData);
        }
        if (this.isOutlier) {
            outputData = this.outlier.transform(outputData);
        }

        return outputData;
    }
}
