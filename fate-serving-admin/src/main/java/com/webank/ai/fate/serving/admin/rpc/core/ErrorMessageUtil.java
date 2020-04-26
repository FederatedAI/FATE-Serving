package com.webank.ai.fate.serving.admin.rpc.core;

import com.webank.ai.fate.serving.admin.bean.Dict;
import com.webank.ai.fate.serving.admin.bean.StatusCode;
import com.webank.ai.fate.serving.admin.exceptions.ShutdownRejectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * @Description TODO
 * @Author
 **/
public class ErrorMessageUtil {

    static Logger logger = LoggerFactory.getLogger(ErrorMessageUtil.class);

    public static Map handleException(Map result, Throwable e) {
        if (e instanceof IllegalArgumentException) {
            result.put(Dict.CODE, StatusCode.PARAM_ERROR);
//            result.put(Dict.MESSAGE, "PARAM_ERROR");
        } else if (e instanceof ShutdownRejectException) {
            result.put(Dict.CODE, StatusCode.SHUTDOWN_ERROR);
//            result.put(Dict.MESSAGE, "SHUTDOWN_ERROR");
        } else {
            logger.error("SYSTEM_ERROR ", e);
            result.put(Dict.CODE, StatusCode.SYSTEM_ERROR);
//            result.put(Dict.MESSAGE, "SYSTEM_ERROR");
        }
        return result;
    }
}
