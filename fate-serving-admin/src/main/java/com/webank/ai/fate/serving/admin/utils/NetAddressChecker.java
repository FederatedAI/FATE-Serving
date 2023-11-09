package com.webank.ai.fate.serving.admin.utils;

import com.webank.ai.fate.serving.admin.services.ComponentService;
import com.webank.ai.fate.serving.core.exceptions.RemoteRpcException;
import com.webank.ai.fate.serving.core.exceptions.SysException;
import com.webank.ai.fate.serving.core.utils.NetUtils;

/**
 * @author hcy
 */
public class NetAddressChecker {

    private static final ComponentService componentService = new ComponentService();

    public static void check(String host, Integer port) {
        if (!NetUtils.isValidAddress(host + ":" + port)) {
            throw new SysException("invalid address");
        }

        if (!componentService.isAllowAccess(host, port)) {
            throw new RemoteRpcException("no allow access, target: " + host + ":" + port);
        }
    }
}
