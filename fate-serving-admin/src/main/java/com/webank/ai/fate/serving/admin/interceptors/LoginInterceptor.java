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

package com.webank.ai.fate.serving.admin.interceptors;

import com.google.common.base.Preconditions;
import com.webank.ai.fate.serving.common.cache.Cache;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

public class LoginInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

    @Autowired
    private Cache cache;

    private static List<String> EXCLUDES = Arrays.asList("/api/component/list", "/api/monitor/queryJvm", "/api/monitor/query", "/api/monitor/queryModel");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getMethod().equals(RequestMethod.OPTIONS.name())) {
            response.setStatus(HttpStatus.OK.value());
            return true;
        }
        String token = request.getHeader(Dict.SESSION_TOKEN);
        Preconditions.checkArgument(StringUtils.isNotBlank(token), "parameter sessionToken is required");

        String userInfo = (String) cache.get(token);
        if (StringUtils.isNotBlank(userInfo)) {
            if (!EXCLUDES.contains(request.getRequestURI())) {
                cache.put(token, userInfo, "local".equalsIgnoreCase(MetaInfo.PROPERTY_CACHE_TYPE) ? MetaInfo.PROPERTY_LOCAL_CACHE_EXPIRE : MetaInfo.PROPERTY_REDIS_EXPIRE);
            }
            return true;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("session token unavailable");
            }
            response.getWriter().write(JsonUtil.object2Json(ReturnResult.build(StatusCode.INVALID_TOKEN, "session token unavailable")));
            response.flushBuffer();
            return false;
        }
    }

}
