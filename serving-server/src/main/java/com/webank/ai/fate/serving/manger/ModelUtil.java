package com.webank.ai.fate.serving.manger;

import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.serving.core.bean.FederatedRoles;
import com.webank.ai.fate.serving.core.bean.ModelInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class ModelUtil {


    private static final String MODEL_KEY_SEPARATOR = "&";


    public static String genModelKey(String name, String namespace) {
        return StringUtils.join(Arrays.asList(name, namespace), MODEL_KEY_SEPARATOR);
    }

    public static String[] splitModelKey(String key) {
        return StringUtils.split(key, MODEL_KEY_SEPARATOR);
    }


    public static FederatedRoles getFederatedRoles(Map<String, ModelServiceProto.Party> federatedRolesProto) {
        FederatedRoles federatedRoles = new FederatedRoles();
        federatedRolesProto.forEach((roleName, party) -> {
            federatedRoles.setRole(roleName, party.getPartyIdList());
        });
        return federatedRoles;
    }

    public static Map<String, Map<String, ModelInfo>> getFederatedRolesModel(Map<String, ModelServiceProto.RoleModelInfo> federatedRolesModelProto) {
        Map<String, Map<String, ModelInfo>> federatedRolesModel = new HashMap<>(8);
        federatedRolesModelProto.forEach((roleName, roleModelInfo) -> {
            federatedRolesModel.put(roleName, new HashMap<>(8));
            roleModelInfo.getRoleModelInfoMap().forEach((partyId, modelInfo) -> {
                federatedRolesModel.get(roleName).put(partyId, new ModelInfo(modelInfo.getTableName(), modelInfo.getNamespace()));
            });
        });
        return federatedRolesModel;
    }


}
