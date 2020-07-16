package com.webank.ai.fate.serving.admin.controller;

import com.webank.ai.fate.serving.admin.services.provider.ValidateServiceProvider;
import com.webank.ai.fate.serving.common.bean.BaseContext;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/api")
@RestController
public class ValidateController {

    private static final Logger logger = LoggerFactory.getLogger(ValidateController.class);

    @Autowired
    ValidateServiceProvider validateServiceProvider;

    // 列出集群中所注册的所有接口
    @PostMapping("/validate/{callName}")
    public ReturnResult validate(@PathVariable String callName, @RequestBody Map params) {
        ReturnResult result = new ReturnResult();
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("try to validate {}, receive params: {}", callName, params);
            }

            BaseContext context = new BaseContext();
            context.setActionType(callName);

            InboundPackage inboundPackage = new InboundPackage();
            inboundPackage.setBody(params);
            OutboundPackage outboundPackage = validateServiceProvider.service(context, inboundPackage);
            Map resultMap = (Map) outboundPackage.getData();

            if (resultMap != null && resultMap.get(Dict.RET_CODE).toString().equals(StatusCode.SUCCESS)) {
                logger.info("validate {} success", callName);
            }

            result.setRetcode(StatusCode.SUCCESS);
            result.setData(resultMap);
        } catch (Exception e) {
            logger.error(e.getMessage());
            result.setRetcode(StatusCode.SYSTEM_ERROR);
            result.setRetmsg(e.getMessage());
        }
        return result;
    }

    /*
     * parameters body
    ================== publishLoad/ publishBind ==================
    {
        "host": "127.0.0.1",
        "port": 8000,
        "serviceId": "666666",
        "local": {
            "role": "guest",
            "partyId": "9999"
        },
        "role": {
            "guest": {
                "partyId": "9999"
            },
            "host": {
                "partyId": "10000"
            },
            "arbiter": {
                "partyId": "10000"
            }
        },
        "model": {
            "guest": {
                "9999": {
                    "tableName": "2020040111152695637611",
                    "namespace": "guest#9999#arbiter-10000#guest-9999#host-10000#model"
                }
            },
            "host": {
                "10000": {
                    "tableName": "2020040111152695637611",
                    "namespace": "host#10000#arbiter-10000#guest-9999#host-10000#model"
                }
            },
            "arbiter": {
                "10000": {
                    "tableName": "2020040111152695637611",
                    "namespace": "arbiter#10000#arbiter-10000#guest-9999#host-10000#model"
                }
            }
        },
        "loadType": "FILE",
        "filePath": "D:/git/FATE-Serving-2.0/fate-serving-server/target/classes/model_2020040111152695637611_guest#9999#arbiter-10000#guest-9999#host-10000#model_cache"
    }

    ================== inference ==================
    {
        "host": "127.0.0.1",
        "port": 8000,
        "serviceId": "666666",
        "featureData": {
            "x0": 0.100016,
            "x1": -1.359293,
            "x2": 2.303601,
            "x3": 2.00137,
            "x4": 1.307686,
            "x7": 0.102345
        },
        "sendToRemoteFeatureData": {
            "device_id": "aaaaa",
            "phone_num": "122222222"
        }
    }
    ================== batchInference ==================
    {
        "host": "127.0.0.1",
        "port": 8000,
        "serviceId": "666666",
        "batchDataList": [
            {
                "featureData": {
                    "x0": 1.88669,
                    "x1": -1.359293,
                    "x2": 2.303601,
                    "x3": 2.00137,
                    "x4": 1.307686
                },
                "sendToRemoteFeatureData": {
                    "device_id": "aaaaa",
                    "phone_num": "122222222"
                }
            },
            {
                "featureData": {
                    "x0": 1.88669,
                    "x1": -1.359293,
                    "x2": 2.303601,
                    "x3": 2.00137,
                    "x4": 1.307686
                },
                "sendToRemoteFeatureData": {
                    "device_id": "aaaaa",
                    "phone_num": "122222222"
                }
            }
        ]
    }
    */

}
