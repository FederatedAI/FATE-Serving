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

package com.webank.ai.fate.serving.core.manager;


import com.webank.ai.fate.serving.core.bean.FederatedRoles;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class FederatedUtils {
    public FederatedUtils() {
    }

    public static String federatedRolesIdentificationString(FederatedRoles federatedRoles) {
        if (federatedRoles == null) {
            return "all";
        } else {
            Object[] roleNames = federatedRoles.getRoleMap().keySet().toArray();
            Arrays.sort(roleNames);
            List<String> allPartyTmp = new ArrayList();

            for (int i = 0; i < roleNames.length; ++i) {
                Object[] partys = (new ArrayList(new HashSet((Collection) federatedRoles.getRoleMap().get(roleNames[i])))).toArray();
                Arrays.sort(partys);
                allPartyTmp.add(StringUtils.join(Arrays.asList(roleNames[i], StringUtils.join(partys, "_")), "-"));
            }

            return StringUtils.join(allPartyTmp, "#");
        }
    }
}
