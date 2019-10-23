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

package com.webank.ai.fate.serving.core.bean;

import com.webank.ai.fate.core.bean.ReturnResult;

public interface Context<Req, Resp> {


    static final String LOGGER_NAME = "flow";

    public void preProcess();

    public Object getData(Object key);

    public Object getDataOrDefault(Object key, Object defaultValue);

    public void putData(Object key, Object data);

    public String getCaseId();

    public void setCaseId(String caseId);

    public long getTimeStamp();

    public default void postProcess(Req req, Resp resp) {
    }

    ;

    public ReturnResult getFederatedResult();

    public void setFederatedResult(ReturnResult returnResult);

    public boolean isHitCache();

    public void hitCache(boolean hitCache);

    public Context subContext();

    public String getActionType();

    public void setActionType(String actionType);

    public String getSeqNo();

    public long getCostTime();


}
