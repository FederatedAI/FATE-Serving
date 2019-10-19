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

package com.webank.ai.fate.serving.interfaces;


import com.webank.ai.fate.core.bean.ReturnResult;
import com.webank.ai.fate.serving.bean.ModelNamespaceData;
import com.webank.ai.fate.serving.core.bean.FederatedParty;
import com.webank.ai.fate.serving.core.bean.FederatedRoles;
import com.webank.ai.fate.serving.core.bean.ModelInfo;
import com.webank.ai.fate.serving.federatedml.PipelineTask;

import java.util.Map;

public interface ModelManager {

    public ReturnResult publishLoadModel(FederatedParty federatedParty, FederatedRoles federatedRoles, Map<String, Map<String, ModelInfo>> federatedRolesModel);

    public ReturnResult publishOnlineModel(FederatedParty federatedParty, FederatedRoles federatedRoles, Map<String, Map<String, ModelInfo>> federatedRolesModel);

    public PipelineTask getModel(String name, String namespace);

    public ModelNamespaceData getModelNamespaceData(String namespace);

    public String getModelNamespaceByPartyId(String partyId);

    public ModelInfo getModelInfoByPartner(String partnerModelName, String partnerModelNamespace);

    public PipelineTask pushModelIntoPool(String name, String namespace);


}
