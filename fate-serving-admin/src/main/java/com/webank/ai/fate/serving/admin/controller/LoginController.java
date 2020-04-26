package com.webank.ai.fate.serving.admin.controller;

import com.google.common.base.Preconditions;
import com.webank.ai.fate.serving.admin.bean.Dict;
import com.webank.ai.fate.serving.admin.bean.ReturnResult;
import com.webank.ai.fate.serving.admin.bean.StatusCode;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Description User management
 * @Date: 2020/3/25 11:13
 * @Author: v_dylanxu
 */
@RequestMapping("/api")
@RestController
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Value("${admin.username}")
    private String username;

    @Value("${admin.password}")
    private String password;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostMapping("/admin/login")
    public ReturnResult login(String username, String password) {
        Preconditions.checkArgument(StringUtils.isNotBlank(username), "parameter username is blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(password), "parameter password is blank");

        if (username.equals(this.username) && password.equals(this.password)) {
            String userInfo = StringUtils.join(Arrays.asList(username, password), "_");
            String token = Md5Crypt.md5Crypt((Dict.USER_CACHE_KEY_PREFIX + userInfo).getBytes(), Dict.MD5_SALT);

            redisTemplate.opsForValue().set(token, userInfo, 10, TimeUnit.MINUTES);
            logger.info("user {} login success.", username);

            Map data = new HashMap<>();
            data.put("timestamp", System.currentTimeMillis());
            data.put("sessionToken", token);
            return ReturnResult.success(data);
        } else {
            logger.info("user {} login failure, username or password is wrong.", username);
            return ReturnResult.failure(StatusCode.USER_ERROR, "username or password is wrong");
        }
    }

    @PostMapping("/admin/logout")
    public ReturnResult logout(HttpServletRequest request) {
        String sessionToken = request.getHeader("sessionToken");
//        "Session token unavailable"
        String userInfo = redisTemplate.opsForValue().get(sessionToken);
        if (StringUtils.isNotBlank(userInfo)) {
            redisTemplate.delete(sessionToken);
            return ReturnResult.success();
        } else {
            logger.info("Session token unavailable");
            return ReturnResult.failure(StatusCode.USER_ERROR, "Session token unavailable");
        }
    }

}
