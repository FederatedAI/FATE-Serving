package com.webank.ai.fate.serving.admin.controller;


import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceGrpc;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.serving.admin.services.ComponentService;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import com.webank.ai.fate.serving.core.bean.RequestParamWrapper;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.utils.HttpClientPool;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description Model management
 * @Date: 2020/3/25 11:13
 * @Author: v_dylanxu
 */
@RequestMapping("/api")
@RestController
public class ModelController {

    private static final Logger logger = LoggerFactory.getLogger(ModelController.class);
    GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();
    @Value("${fateflow.load.url}")
    private String loadUrl;
    @Value("${fateflow.bind.url}")
    private String bindUrl;
    @Value("${grpc.timeout:5000}")
    private int timeout;

    @Autowired
    private ComponentService componentService;

    @GetMapping("/model/query")
    public ReturnResult queryModel(String host, int port, String serviceId, Integer page, Integer pageSize) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != 0, "parameter port is blank");

        if (page == null || page < 0) {
            page = 1;
        }

        if (pageSize == null) {
            pageSize = 10;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("query model, host: {}, port: {}, serviceId: {}", host, port, serviceId);
        }

        ModelServiceGrpc.ModelServiceBlockingStub blockingStub = getModelServiceBlockingStub(host, port);

        ModelServiceProto.QueryModelRequest.Builder queryModelRequestBuilder = ModelServiceProto.QueryModelRequest.newBuilder();

        if (StringUtils.isNotBlank(serviceId)) {
            // by service id
            queryModelRequestBuilder.setQueryType(1);
//            queryModelRequestBuilder.setTableName(tableName);
//            queryModelRequestBuilder.setNamespace(namespace);
            queryModelRequestBuilder.setServiceId(serviceId);
        } else {
            // list all
            queryModelRequestBuilder.setQueryType(0);
        }

        ModelServiceProto.QueryModelResponse response = blockingStub.queryModel(queryModelRequestBuilder.build());

        if (logger.isDebugEnabled()) {
            logger.debug("response: {}", response);
        }

        Map data = Maps.newHashMap();
        List rows = Lists.newArrayList();
        List<ModelServiceProto.ModelInfoEx> modelInfosList = response.getModelInfosList();
        int totalSize = 0;
        if (modelInfosList != null) {
            totalSize = modelInfosList.size();
            modelInfosList = modelInfosList.stream().sorted(Comparator.comparingInt(ModelServiceProto.ModelInfoEx::getIndex)).collect(Collectors.toList());

            // Pagination
            int totalPage = (modelInfosList.size() + pageSize - 1) / pageSize;
            if (page <= totalPage) {
                modelInfosList = modelInfosList.subList((page - 1) * pageSize, Math.min(page * pageSize, modelInfosList.size()));
            }

            for (ModelServiceProto.ModelInfoEx modelInfoEx : modelInfosList) {
                rows.add(JsonUtil.json2Object(modelInfoEx.getContent(), Map.class));
            }
        }

        data.put("total", totalSize);
        data.put("rows", rows);
//        return response;
        return ReturnResult.build(response.getRetcode(), response.getMessage(), data);
    }

    @PostMapping("/model/publishLoad")
    public Callable<ReturnResult> publishLoad(@RequestBody String requestData) {
        return () -> {
            if (logger.isDebugEnabled()) {
                logger.debug("try to publishLoad, receive : {}", requestData);
            }
            ReturnResult result = new ReturnResult();
            Map data = JsonUtil.json2Object(requestData, Map.class);
            Preconditions.checkArgument(data.get(Dict.PARAMS_INITIATOR) != null, "parameter initiator not exist");
            Preconditions.checkArgument(data.get(Dict.PARAMS_ROLE) != null, "parameter role not exist");
            Preconditions.checkArgument(data.get(Dict.PARAMS_JOB_PARAMETERS) != null, "parameter job_parameters not exist");

            String resp = HttpClientPool.post(loadUrl, data, null);

            logger.info("publishLoad response : {}", resp);

            if (StringUtils.isNotBlank(resp)) {
                result.setRetcode(StatusCode.SUCCESS);
                result.setData(JsonUtil.json2Object(resp, Map.class));
            } else {
                result.setRetcode(StatusCode.GUEST_LOAD_MODEL_ERROR);
                result.setRetmsg("publishLoad failed");
            }
            return result;
        };
    }

    @PostMapping("/model/publishBind")
    public Callable<ReturnResult> publishBind(@RequestBody String requestData) {
        return () -> {
            if (logger.isDebugEnabled()) {
                logger.debug("try to publishBind, receive : {}", requestData);
            }
            ReturnResult result = new ReturnResult();

            Map data = JsonUtil.json2Object(requestData, Map.class);
            Preconditions.checkArgument(data.get(Dict.PARAMS_INITIATOR) != null, "parameter initiator not exist");
            Preconditions.checkArgument(data.get(Dict.PARAMS_ROLE) != null, "parameter role not exist");
            Preconditions.checkArgument(data.get(Dict.PARAMS_JOB_PARAMETERS) != null, "parameter job_parameters not exist");
            Preconditions.checkArgument(data.get(Dict.PARAMS_SERVICE_ID) != null, "parameter service_id not exist");

            String resp = HttpClientPool.post(bindUrl, data);

            logger.info("publishBind response : {}", resp);

            if (StringUtils.isNotBlank(resp)) {
                result.setRetcode(StatusCode.SUCCESS);
                result.setData(JsonUtil.json2Object(resp, Map.class));
            } else {
                result.setRetcode(StatusCode.GUEST_BIND_MODEL_ERROR);
                result.setRetmsg("publishBind failed");
            }
            return result;
        };
    }

    @PostMapping("/model/unload")
    public Callable<ReturnResult> unload(@RequestBody RequestParamWrapper requestParams) throws Exception {
        return () -> {
            String host = requestParams.getHost();
            Integer port = requestParams.getPort();
            String tableName = requestParams.getTableName();
            String namespace = requestParams.getNamespace();

            Preconditions.checkArgument(StringUtils.isNotBlank(tableName), "parameter tableName is blank");
            Preconditions.checkArgument(StringUtils.isNotBlank(namespace), "parameter namespace is blank");

            ReturnResult result = new ReturnResult();

            if (logger.isDebugEnabled()) {
                logger.debug("unload model by tableName and namespace, host: {}, port: {}, tableName: {}, namespace: {}", host, port, tableName, namespace);
            }

            ModelServiceGrpc.ModelServiceFutureStub futureStub = getModelServiceFutureStub(host, port);

            ModelServiceProto.UnloadRequest unloadRequest = ModelServiceProto.UnloadRequest.newBuilder()
                    .setTableName(tableName)
                    .setNamespace(namespace)
                    .build();

            ListenableFuture<ModelServiceProto.UnloadResponse> future = futureStub.unload(unloadRequest);
            ModelServiceProto.UnloadResponse response = future.get(timeout, TimeUnit.MILLISECONDS);

            if (logger.isDebugEnabled()) {
                logger.debug("response: {}", response);
            }

            result.setRetcode(response.getStatusCode());
//            result.setData(JSONObject.parseObject(response.getData().toStringUtf8()));
            result.setRetmsg(response.getMessage());
            return result;
        };
    }

    @PostMapping("/model/unbind")
    public Callable<ReturnResult> unbind(@RequestBody RequestParamWrapper requestParams) throws Exception {
        return () -> {
            String host = requestParams.getHost();
            Integer port = requestParams.getPort();
            String tableName = requestParams.getTableName();
            String namespace = requestParams.getNamespace();
            String serviceId = requestParams.getServiceId();

            Preconditions.checkArgument(StringUtils.isNotBlank(tableName), "parameter tableName is blank");
            Preconditions.checkArgument(StringUtils.isNotBlank(namespace), "parameter namespace is blank");
            Preconditions.checkArgument(StringUtils.isNotBlank(serviceId), "parameter serviceId is blank");

            ReturnResult result = new ReturnResult();

            if (logger.isDebugEnabled()) {
                logger.debug("unload model by tableName and namespace, host: {}, port: {}, tableName: {}, namespace: {}", host, port, tableName, namespace);
            }

            ModelServiceGrpc.ModelServiceFutureStub futureStub = getModelServiceFutureStub(host, port);

            ModelServiceProto.UnbindRequest unbindRequest = ModelServiceProto.UnbindRequest.newBuilder()
                    .setTableName(tableName)
                    .setNamespace(namespace)
                    .setServiceId(serviceId)
                    .build();

            ListenableFuture<ModelServiceProto.UnbindResponse> future = futureStub.unbind(unbindRequest);
            ModelServiceProto.UnbindResponse response = future.get(timeout, TimeUnit.MILLISECONDS);

            if (logger.isDebugEnabled()) {
                logger.debug("response: {}", response);
            }

            result.setRetcode(String.valueOf(response.getStatusCode()));
            result.setRetmsg(response.getMessage());
            return result;
        };
    }

    private ModelServiceGrpc.ModelServiceBlockingStub getModelServiceBlockingStub(String host, Integer port) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != null && port.intValue() != 0, "parameter port was wrong");

        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        ModelServiceGrpc.ModelServiceBlockingStub blockingStub = ModelServiceGrpc.newBlockingStub(managedChannel);
        blockingStub = blockingStub.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS);
        return blockingStub;
    }

    private ModelServiceGrpc.ModelServiceFutureStub getModelServiceFutureStub(String host, Integer port) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != null && port.intValue() != 0, "parameter port was wrong");

        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        ModelServiceGrpc.ModelServiceFutureStub futureStub = ModelServiceGrpc.newFutureStub(managedChannel);
        return futureStub;
    }

}
