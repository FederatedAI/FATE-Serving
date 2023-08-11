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

public class BatchHostFederatedParams extends BatchInferenceRequest {

    String hostTableName;
    String hostNamespace;
    String guestPartyId;
    String hostPartyId;
    String caseId;

    public String getHostTableName() {
        return hostTableName;
    }

    public void setHostTableName(String hostTableName) {
        this.hostTableName = hostTableName;
    }

    public String getHostNamespace() {
        return hostNamespace;
    }

    public void setHostNamespace(String hostNamespace) {
        this.hostNamespace = hostNamespace;
    }

    public String getGuestPartyId() {
        return guestPartyId;
    }

    public void setGuestPartyId(String guestPartyId) {
        this.guestPartyId = guestPartyId;
    }

    public String getHostPartyId() {
        return hostPartyId;
    }

    public void setHostPartyId(String hostPartyId) {
        this.hostPartyId = hostPartyId;
    }

    @Override
    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

}
