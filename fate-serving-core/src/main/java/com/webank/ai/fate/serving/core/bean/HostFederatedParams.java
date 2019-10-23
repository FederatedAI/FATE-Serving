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

import java.util.Map;

/**
 * @Description TODO
 * @Author kaideng
 **/
public class HostFederatedParams extends FederatedParams {

//    Map<String, Object> requestData = new HashMap<>();
//        Arrays.asList("caseid", "seqno").forEach((field -> {
//        requestData.put(field, federatedParams.get(field));
//    }));
//        requestData.put("partner_local", ObjectTransform.bean2Json(srcParty));
//        requestData.put("partner_model_info", ObjectTransform.bean2Json(federatedParams.get("model_info")));
//        requestData.put("feature_id", ObjectTransform.bean2Json(federatedParams.get("feature_id")));
//        requestData.put("local", ObjectTransform.bean2Json(dstParty));
//        requestData.put("role", ObjectTransform.bean2Json(federatedParams.get("role")));


    //    private  String caseId;
//    private  String seqNo;
    protected FederatedParty partnerLocal;
    protected String methodName;
    protected ModelInfo partnerModelInfo;

    public HostFederatedParams() {

    }
    //   private  FederatedParty local;

    public HostFederatedParams(String caseId, String seqNo, FederatedParty partnerLocal, FederatedParty local, FederatedRoles role, Map<String, Object> featureIdMap) {
        this.caseId = caseId;
        this.seqNo = seqNo;
        this.partnerLocal = partnerLocal;
        this.local = local;
        this.role = role;
        this.featureIdMap = featureIdMap;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public ModelInfo getPartnerModelInfo() {
        return partnerModelInfo;
    }

    public void setPartnerModelInfo(ModelInfo partnerModelInfo) {
        this.partnerModelInfo = partnerModelInfo;
    }

    public FederatedParty getPartnerLocal() {
        return partnerLocal;
    }

    public void setPartnerLocal(FederatedParty partnerLocal) {
        this.partnerLocal = partnerLocal;
    }


}
