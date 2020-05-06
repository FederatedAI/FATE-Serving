package com.webank.ai.fate.serving.admin.interceptors;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

public class LoginInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        String token = request.getHeader("sessionToken");
//        Preconditions.checkArgument(StringUtils.isNotBlank(token), "parameter sessionToken is required");
//
//        String userInfo = redisTemplate.opsForValue().get(token);
//        if (StringUtils.isNotBlank(userInfo)) {
//            // reset ttl
//            redisTemplate.opsForValue().set(token, userInfo, 10, TimeUnit.MINUTES);
//            return true;
//        } else {
//            logger.info("Session token unavailable");
//            ReturnResult result = new ReturnResult();
//            result.setRetcode(StatusCode.PARAM_ERROR);
//            result.setRetmsg("Session token unavailable");
//            response.getWriter().write(JSONObject.toJSONString(result));
//            response.flushBuffer();
//            return false;
//        }
        return true;
    }

}
