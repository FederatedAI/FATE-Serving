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

import com.webank.ai.fate.serving.common.model.MergeInferenceAware;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.GuestMergeException;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class HeteroLinRGuest extends HeteroLR implements MergeInferenceAware, Returnable {

    private static final Logger logger = LoggerFactory.getLogger(HeteroLinRGuest.class);

    @Override
    public Map<String, Object> localInference(Context context, List<Map<String, Object>> input
    ) {
        Map<String, Object> result = new HashMap<>(8);
        Map<String, Double> forwardRet = forward(input);
        double score = forwardRet.get(Dict.SCORE);
        result.put(Dict.SCORE, score);
        return result;
    }

    @Override
    public Map<String, Object> mergeRemoteInference(Context context, List<Map<String, Object>> guestData,
                                                    Map<String, Object> hostData) {
        Map<String, Object> result = this.handleRemoteReturnData(hostData);
        if ((int) result.get(Dict.RET_CODE) == StatusCode.SUCCESS) {
            if (CollectionUtils.isNotEmpty(guestData)) {
                AtomicReference<Double> score = new AtomicReference<>((double) 0);
                Map<String, Object> tempMap = guestData.get(0);
                Map<String, Object> componentData = (Map<String, Object>) tempMap.get(this.getComponentName());
                double localScore = 0;
                if (componentData != null && componentData.get(Dict.SCORE) != null) {
                    localScore = ((Number) componentData.get(Dict.SCORE)).doubleValue();
                } else {
                    throw new GuestMergeException("local result is invalid ");
                }
                score.set(localScore);

                hostData.forEach((k, v) -> {
                    Map<String, Object> onePartyData = (Map<String, Object>) v;

                    Map<String, Object> remoteComponentData = (Map<String, Object>) onePartyData.get(this.getComponentName());
                    double remoteScore;
                    if (remoteComponentData != null) {
                        remoteScore = ((Number) remoteComponentData.get(Dict.SCORE)).doubleValue();
                    } else {
                        if (onePartyData.get(Dict.PROB) != null) {
                            remoteScore = ((Number) onePartyData.get(Dict.PROB)).doubleValue();
                        } else {
                            throw new GuestMergeException("host data score is null");
                        }
                    }
                    score.updateAndGet(v1 -> new Double((double) (v1 + remoteScore)));
                });
                result.put(Dict.SCORE, score);

            }
        }
        return result;
    }


}
