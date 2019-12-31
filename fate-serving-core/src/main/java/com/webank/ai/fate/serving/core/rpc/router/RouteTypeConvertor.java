package com.webank.ai.fate.serving.core.rpc.router;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description TODO
 * @Author
 **/

public class RouteTypeConvertor {
    private static final String  ROUTE_TYPE_RANDOM ="random";
    private static final String  ROUTE_TYPE_CONSISTENT_HASH ="consistent";

    private static final Logger logger  = LoggerFactory.getLogger(RouteTypeConvertor.class);

    public static RouteType string2RouteType(String routeTypeString) {
        RouteType routeType = RouteType.RANDOM_ROUTE;
        if(StringUtils.isNotEmpty(routeTypeString)){
            if(routeTypeString.equalsIgnoreCase(ROUTE_TYPE_RANDOM)){
                routeType = RouteType.RANDOM_ROUTE;
            }
            else if(routeTypeString.equalsIgnoreCase(ROUTE_TYPE_CONSISTENT_HASH)){
                routeType = RouteType.CONSISTENT_HASH_ROUTE;
            }
            else{
                routeType = RouteType.RANDOM_ROUTE;
                logger.error("unknown routeType{}, will use {} instead.", routeTypeString, ROUTE_TYPE_RANDOM);
            }
        }
        return routeType;
    }
}
