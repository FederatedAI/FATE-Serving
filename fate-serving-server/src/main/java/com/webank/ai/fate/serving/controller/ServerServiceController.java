package com.webank.ai.fate.serving.controller;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.api.networking.common.CommonServiceGrpc;
import com.webank.ai.fate.api.networking.common.CommonServiceProto;
import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.bean.RequestParamWrapper;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.exceptions.SysException;
import com.webank.ai.fate.serving.core.utils.NetUtils;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * @author hcy
 */
@RestController
public class ServerServiceController {

    Logger logger = LoggerFactory.getLogger(ServerServiceController.class);

    GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();

    @RequestMapping(value = "/server/service/weight/update", method = RequestMethod.POST)
    @ResponseBody
    public ReturnResult updateService(@RequestBody RequestParamWrapper requestParams) throws Exception {
        String host = requestParams.getHost();
        int port = requestParams.getPort();
        String url = requestParams.getUrl();
        String routerMode = requestParams.getRouterMode();
        Integer weight = requestParams.getWeight();
        Long version = requestParams.getVersion();

        if (logger.isDebugEnabled()) {
            logger.debug("try to update service");
        }

        Preconditions.checkArgument(StringUtils.isNotBlank(url), "parameter url is blank");

        logger.info("update url: {}, routerMode: {}, weight: {}, version: {}", url, routerMode, weight, version);

        CommonServiceGrpc.CommonServiceFutureStub commonServiceFutureStub = getCommonServiceFutureStub(host, port);
        CommonServiceProto.UpdateServiceRequest.Builder builder = CommonServiceProto.UpdateServiceRequest.newBuilder();

        builder.setUrl(url);
        if (StringUtils.isNotBlank(routerMode)) {
            builder.setRouterMode(routerMode);
        }

        if (weight != null) {
            builder.setWeight(weight);
        } else {
            builder.setWeight(-1);
        }

        if (version != null) {
            builder.setVersion(version);
        } else {
            builder.setVersion(-1);
        }

        ListenableFuture<CommonServiceProto.CommonResponse> future = commonServiceFutureStub.updateService(builder.build());

        CommonServiceProto.CommonResponse response = future.get(MetaInfo.PROPERTY_GRPC_TIMEOUT, TimeUnit.MILLISECONDS);

        ReturnResult result = new ReturnResult();
        result.setRetcode(response.getStatusCode());
        result.setRetmsg(response.getMessage());
        return result;
    }

    private CommonServiceGrpc.CommonServiceFutureStub getCommonServiceFutureStub(String host, Integer port) {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != null && port != 0, "parameter port was wrong");

        if (!NetUtils.isValidAddress(host + ":" + port)) {
            throw new SysException("invalid address");
        }

        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        return CommonServiceGrpc.newFutureStub(managedChannel);
    }
}
