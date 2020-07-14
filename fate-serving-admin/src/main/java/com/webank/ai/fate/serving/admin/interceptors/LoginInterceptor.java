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

public class LoginInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

    @Autowired
    private Cache cache;

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
            cache.put(token, userInfo, MetaInfo.PROPERTY_CACHE_TYPE.equalsIgnoreCase("local") ? MetaInfo.PROPERTY_LOCAL_CACHE_EXPIRE : MetaInfo.PROPERTY_REDIS_EXPIRE);
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
