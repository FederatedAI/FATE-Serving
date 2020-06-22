package com.webank.ai.fate.serving.admin.services.interceptors;

import com.google.common.base.Preconditions;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.Interceptor;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.BaseException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ValidateParamInterceptor implements Interceptor {

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
        try {
            Map params = (Map) inboundPackage.getBody();
            String host = (String) params.get(Dict.HOST);
            Integer port = (Integer) params.get(Dict.PORT);

            Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is required");
            Preconditions.checkArgument(port != null && port.intValue() != 0, "parameter port is required");
        } catch (Exception e) {
            throw new BaseException(StatusCode.PARAM_ERROR, e.getMessage());
        }
    }
}
