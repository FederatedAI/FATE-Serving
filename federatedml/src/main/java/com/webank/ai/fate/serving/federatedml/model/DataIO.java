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
import com.webank.ai.fate.core.mlmodel.buffer.DataIOMetaProto.DataIOMeta;
import com.webank.ai.fate.core.mlmodel.buffer.DataIOParamProto.DataIOParam;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.FederatedParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class DataIO extends BaseModel {
    private static final Logger LOGGER = LogManager.getLogger();
    private DataIOMeta dataIOMeta;
    private DataIOParam dataIOParam;
    private Imputer imputer;
    private Outlier outlier;
    private boolean isImputer;
    private boolean isOutlier;

    @Override
    public int initModel(byte[] protoMeta, byte[] protoParam) {
        LOGGER.info("start init DataIO class");
        try {
            this.dataIOMeta = this.parseModel(DataIOMeta.parser(), protoMeta);
            this.dataIOParam = this.parseModel(DataIOParam.parser(), protoParam);
            this.isImputer = this.dataIOMeta.getImputerMeta().getIsImputer();
            LOGGER.info("data io isImputer {}", this.isImputer);
            if (this.isImputer) {
                this.imputer = new Imputer(this.dataIOMeta.getImputerMeta().getMissingValueList(),
                        this.dataIOParam.getImputerParam().getMissingReplaceValue());
            }

            this.isOutlier = this.dataIOMeta.getOutlierMeta().getIsOutlier();
            LOGGER.info("data io isOutlier {}", this.isOutlier);
            if (this.isOutlier) {
                this.outlier = new Outlier(this.dataIOMeta.getOutlierMeta().getOutlierValueList(),
                        this.dataIOParam.getOutlierParam().getOutlierReplaceValue());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return StatusCode.ILLEGALDATA;
        }
        LOGGER.info("Finish init DataIO class");
        return StatusCode.OK;
    }

    @Override
    public Map<String, Object> handlePredict(Context context, List<Map<String, Object>> inputData, FederatedParams predictParams) {
        Map<String, Object> input = inputData.get(0);

        if (this.isImputer) {
            input = this.imputer.transform(input);
        }

        if (this.isOutlier) {
            input = this.outlier.transform(input);
        }

        return input;
    }
}
