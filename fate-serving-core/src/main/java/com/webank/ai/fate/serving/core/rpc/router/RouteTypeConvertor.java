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

package com.webank.ai.fate.serving.core.rpc.router;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description
 * @Author
 **/

public class RouteTypeConvertor {
    private static final String ROUTE_TYPE_RANDOM = "random";
    private static final String ROUTE_TYPE_CONSISTENT_HASH = "consistent";
    private static final String ROUND_ROBIN = "round_robin";

    private static final Logger logger = LoggerFactory.getLogger(RouteTypeConvertor.class);

    public static RouteType string2RouteType(String routeTypeString) {
        if (StringUtils.isNotEmpty(routeTypeString)) {
            if (routeTypeString.equalsIgnoreCase(ROUTE_TYPE_CONSISTENT_HASH)) {
                return RouteType.CONSISTENT_HASH_ROUTE;
            } else if (routeTypeString.equalsIgnoreCase(ROUND_ROBIN)) {
                return RouteType.ROUND_ROBIN;
            } else if (!routeTypeString.equalsIgnoreCase(ROUTE_TYPE_RANDOM)) {
                logger.error("unknown routeType{}, will use {} instead.", routeTypeString, ROUTE_TYPE_RANDOM);
            }
        }
        return RouteType.RANDOM_ROUTE;
    }
}
