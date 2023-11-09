package com.webank.ai.fate.serving.controller;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceGrpc;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author hcy
 */
@RestController
public class ServerModelController {

    Logger logger = LoggerFactory.getLogger(ServerModelController.class);

    GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();

    @RequestMapping(value = "/server/model/unbind", method = RequestMethod.POST)
    @ResponseBody
    public Callable<ReturnResult> unbind(@RequestBody RequestParamWrapper requestParams) throws Exception {
        return () -> {
            String host = requestParams.getHost();
            Integer port = requestParams.getPort();
            String tableName = requestParams.getTableName();
            String namespace = requestParams.getNamespace();
            List<String> serviceIds = requestParams.getServiceIds();

            Preconditions.checkArgument(StringUtils.isNotBlank(tableName), "parameter tableName is blank");
            Preconditions.checkArgument(StringUtils.isNotBlank(namespace), "parameter namespace is blank");
            Preconditions.checkArgument(serviceIds != null && serviceIds.size() != 0, "parameter serviceId is blank");

            ReturnResult result = new ReturnResult();

            logger.debug("unbind model by tableName and namespace, host: {}, port: {}, tableName: {}, namespace: {}", host, port, tableName, namespace);

            ModelServiceGrpc.ModelServiceFutureStub futureStub = getModelServiceFutureStub(host, port);

            ModelServiceProto.UnbindRequest unbindRequest = ModelServiceProto.UnbindRequest.newBuilder()
                    .setTableName(tableName)
                    .setNamespace(namespace)
                    .addAllServiceIds(serviceIds)
                    .build();

            ListenableFuture<ModelServiceProto.UnbindResponse> future = futureStub.unbind(unbindRequest);

            ModelServiceProto.UnbindResponse response = future.get(MetaInfo.PROPERTY_GRPC_TIMEOUT, TimeUnit.MILLISECONDS);

            logger.debug("response: {}", response);

            result.setRetcode(response.getStatusCode());
            result.setRetmsg(response.getMessage());
            return result;
        };
    }

    @RequestMapping(value = "/server/model/transfer", method = RequestMethod.POST)
    @ResponseBody
    public Callable<ReturnResult> transfer(@RequestBody RequestParamWrapper requestParams) {
        return () -> {
            String host = requestParams.getHost();
            Integer port = requestParams.getPort();
            String tableName = requestParams.getTableName();
            String namespace = requestParams.getNamespace();

            String targetHost = requestParams.getTargetHost();
            Integer targetPort = requestParams.getTargetPort();

            Preconditions.checkArgument(StringUtils.isNotBlank(tableName), "parameter tableName is blank");
            Preconditions.checkArgument(StringUtils.isNotBlank(namespace), "parameter namespace is blank");

            ReturnResult result = new ReturnResult();

            logger.debug("transfer model by tableName and namespace, host: {}, port: {}, tableName: {}, namespace: {}, targetHost: {}, targetPort: {}"
                    , host, port, tableName, namespace, targetHost, targetPort);

            ModelServiceGrpc.ModelServiceFutureStub futureStub = getModelServiceFutureStub(targetHost, targetPort);
            ModelServiceProto.FetchModelRequest fetchModelRequest = ModelServiceProto.FetchModelRequest.newBuilder()
                    .setNamespace(namespace).setTableName(tableName).setSourceIp(host).setSourcePort(port).build();

            ListenableFuture<ModelServiceProto.FetchModelResponse> future = futureStub.fetchModel(fetchModelRequest);
            ModelServiceProto.FetchModelResponse response = future.get(MetaInfo.PROPERTY_GRPC_TIMEOUT, TimeUnit.MILLISECONDS);

            logger.debug("response: {}", response);

            result.setRetcode(response.getStatusCode());
            result.setRetmsg(response.getMessage());
            return result;
        };
    }

    private ModelServiceGrpc.ModelServiceFutureStub getModelServiceFutureStub(String host, Integer port) {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != null && port != 0, "parameter port was wrong");

        if (!NetUtils.isValidAddress(host + ":" + port)) {
            throw new SysException("invalid address");
        }

        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        return ModelServiceGrpc.newFutureStub(managedChannel);
    }
}
