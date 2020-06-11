package com.webank.ai.fate.serving.admin.controller;

import com.google.common.base.Preconditions;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.EncryptMethod;
import com.webank.ai.fate.serving.core.bean.RequestParamWrapper;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.cache.Cache;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.utils.EncryptUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
    private Cache cache;

    @PostMapping("/admin/login")
    public ReturnResult login(@RequestBody RequestParamWrapper requestParams) {
        String username = requestParams.getUsername();
        String password = requestParams.getPassword();

        Preconditions.checkArgument(StringUtils.isNotBlank(username), "parameter username is blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(password), "parameter password is blank");

        ReturnResult result = new ReturnResult();
        if (username.equals(this.username) && password.equals(this.password)) {
            String userInfo = StringUtils.join(Arrays.asList(username, password), "_");
//            String token = Md5Crypt.md5Crypt((Dict.USER_CACHE_KEY_PREFIX + userInfo).getBytes(), Dict.MD5_SALT);
            String token = EncryptUtils.encrypt(Dict.USER_CACHE_KEY_PREFIX + userInfo, EncryptMethod.MD5);
            cache.put(token, userInfo, 600);
            logger.info("user {} login success.", username);

            Map data = new HashMap<>();
            data.put("timestamp", System.currentTimeMillis());
            data.put(Dict.SESSION_TOKEN, token);
            result.setRetcode(StatusCode.SUCCESS);
            result.setData(data);
        } else {
            logger.info("user {} login failure, username or password is wrong.", username);
            result.setRetcode(StatusCode.PARAM_ERROR);
            result.setRetmsg("username or password is wrong");
        }
        return result;
    }

    @PostMapping("/admin/logout")
    public ReturnResult logout(HttpServletRequest request) {
        ReturnResult result = new ReturnResult();

        String sessionToken = request.getHeader(Dict.SESSION_TOKEN);
        String userInfo = (String) cache.get(sessionToken);
        if (StringUtils.isNotBlank(userInfo)) {
            cache.delete(sessionToken);
            result.setRetcode(StatusCode.SUCCESS);
        } else {
            logger.info("Session token unavailable");
            result.setRetcode(StatusCode.PARAM_ERROR);
            result.setRetmsg("Session token unavailable");
        }
        return result;
    }

}
