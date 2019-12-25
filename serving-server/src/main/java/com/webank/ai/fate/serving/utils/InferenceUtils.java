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

package com.webank.ai.fate.serving.utils;



import com.webank.ai.eggroll.core.utils.ObjectTransform;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.manager.FederatedUtils;
import com.webank.ai.fate.serving.core.utils.GetSystemInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InferenceUtils {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Logger inferenceAuditLogger = LogManager.getLogger(Dict.INFERENCE_AUDIT);
    private static final Logger inferenceLogger = LogManager.getLogger(Dict.INFERENCE);

    public static String generateCaseid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateSeqno() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static void logInference(Context context, Enum<FederatedInferenceType> inferenceType, FederatedParty federatedParty, FederatedRoles federatedRoles, String caseid, String seqno, int retcode, long elapsed, boolean getRemotePartyResult, boolean billing, Map<String, Object> inferenceRequest, ReturnResult inferenceResult) {
        inferenceAuditLogger.info("{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", GetSystemInfo.localIp, inferenceType, federatedParty.getRole(), federatedParty.getPartyId(), FederatedUtils.federatedRolesIdentificationString(federatedRoles), caseid, seqno, retcode, elapsed, getRemotePartyResult ? 1 : 0, billing ? 1 : 0, context.isHitCache());
        Map<String, Object> inferenceLog = new HashMap<>(8);
        inferenceLog.put(Dict.INFERENCE_REQUEST, inferenceRequest);
        inferenceLog.put(Dict.INFERENCE_RESULT, ObjectTransform.bean2Json(inferenceResult));
        String inferenceLogBase64String = Base64.getEncoder().encodeToString(ObjectTransform.bean2Json(inferenceLog).getBytes());
        inferenceLogger.info("{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", GetSystemInfo.localIp, inferenceType, federatedParty.getRole(), federatedParty.getPartyId(), FederatedUtils.federatedRolesIdentificationString(federatedRoles), caseid, seqno, retcode, elapsed, getRemotePartyResult ? 1 : 0, billing ? 1 : 0, context.isHitCache(), inferenceLogBase64String);
    }

    public static Object getClassByName(String classPath) {
        try {
            Class thisClass = Class.forName(classPath);
            return thisClass.getConstructor().newInstance();
        } catch (ClassNotFoundException ex) {
            LOGGER.error("Can not found this class: {}.", classPath);
        } catch (NoSuchMethodException ex) {
            LOGGER.error("Can not get this class({}) constructor.", classPath);
        } catch (Exception ex) {
            LOGGER.error(ex);
            LOGGER.error("Can not create class({}) instance.", classPath);
        }
        return null;
    }
}
