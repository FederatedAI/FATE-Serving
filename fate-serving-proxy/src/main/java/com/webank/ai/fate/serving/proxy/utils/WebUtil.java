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

package com.webank.ai.fate.serving.proxy.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Description TODO
 * @Author
 **/
public class WebUtil {

    static Logger logger = LoggerFactory.getLogger(WebUtil.class);

    /**
     * 获取来源ip
     *
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request) {

        String ipAddress = "";
        try {
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                String localIp = "127.0.0.1";
                String localIpv6 = "0:0:0:0:0:0:0:1";
                if (ipAddress.equals(localIp) || ipAddress.equals(localIpv6)) {
                    // 根据网卡取本机配置的IP
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                        ipAddress = inet.getHostAddress();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
            }
            // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            String ipSeparate = ",";
            int ipLength = 15;
            if (ipAddress != null && ipAddress.length() > ipLength) {
                if (ipAddress.indexOf(ipSeparate) > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(ipSeparate));
                }
            }
        } catch (Exception e) {
            logger.error("get remote ip error", e);
        }

        return ipAddress;
    }

}
