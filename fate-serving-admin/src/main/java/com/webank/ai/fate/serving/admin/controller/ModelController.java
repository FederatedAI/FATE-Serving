package com.webank.ai.fate.serving.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceGrpc;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.serving.admin.config.FateServiceRegister;
import com.webank.ai.fate.serving.admin.services.ComponentService;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.utils.HttpClientPool;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Value("${grpc.timeout:5000}")
    private int timeout;
    @Autowired
    private FateServiceRegister fateServiceRegister;

    @Autowired
    private ComponentService  componentService;

//    @RequestMapping(value = "/model/{version}/{callName}", method = {RequestMethod.GET, RequestMethod.POST})
//    @ResponseBody
//    public Callable<ReturnResult> modelQuery(@PathVariable String version, @PathVariable String callName,
//                                       @RequestBody String data, @RequestHeader HttpHeaders headers,
//                                       HttpServletRequest httpServletRequest
//    ) throws Exception {
//        return () -> {
//            if (logger.isDebugEnabled()) {
//                logger.debug("receive : {} headers {}", data, headers.toSingleValueMap());
//            }
//
//            Context context = new BaseContext();
//            context.setCallName(callName);
//            context.setVersion(version);
//
//            final ServiceAdaptor serviceAdaptor = fateServiceRegister.getServiceAdaptor(Dict.SERVICE_NAME_MODEL_QUERY);
//            if (serviceAdaptor == null) {
//                throw new RemoteRpcException(StatusCode.SYSTEM_ERROR, "service not found");
//            }
//
//            InboundPackage<Map> inboundPackage = buildInboundPackage(context, data, headers, httpServletRequest);
//
//            OutboundPackage<ReturnResult> result = serviceAdaptor.service(context, inboundPackage);
//            return result.getData();
//        };
//    }

    private InboundPackage<Map> buildInboundPackage(Context context, String data, HttpHeaders headers, HttpServletRequest httpServletRequest) {
        try {
            Map head = Maps.newHashMap();
//            head.put(Dict.HOST, headers.getFirst(Dict.HOST) != null ? headers.getFirst(Dict.HOST).trim() : "");

            Map body = JSON.parseObject(data);

            InboundPackage<Map> inboundPackage = new InboundPackage<>();
            inboundPackage.setBody(body);
            inboundPackage.setHead(head);
            inboundPackage.setSource(data);
            return inboundPackage;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 实现queryModel
     *
     * @return
     * @throws Exception
     */
//    @RequestMapping(value = "/model/{version}/{callName}", method = {RequestMethod.GET, RequestMethod.POST})
//    @ResponseBody
//    public Callable<ReturnResult> model(@PathVariable String version,
//                                        @PathVariable String callName,
//                                        @RequestBody String data,
//                                        @RequestHeader HttpHeaders headers,
//                                        HttpServletRequest httpServletRequest
//    ) throws Exception {
//        return () -> {
//            if (logger.isDebugEnabled()) {
//                logger.debug("callName: {} receive: {} headers: {}", callName, data, headers.toSingleValueMap());
//            }
//            Context context = new BaseContext();
//
//            InboundPackage<Map> inboundPackage = buildInboundPackage(context, data, headers, httpServletRequest);
//            Map body = inboundPackage.getBody();
//
//            String host = body.get(Dict.HOST) != null ? body.get(Dict.HOST).toString() : null;
//            Integer port = body.get(Dict.PORT) != null ? Integer.valueOf(body.get(Dict.PORT).toString()) : null;
//
//            Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
//            Preconditions.checkArgument(port != null, "parameter port is null");
//
//            ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(context.geth(), context.getPort());
//            ModelServiceGrpc.ModelServiceBlockingStub blockingStub = ModelServiceGrpc.newBlockingStub(managedChannel);
//
//            blockingStub = blockingStub.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS);
//
//            ModelServiceProto.PublishRequest.Builder builder = ModelServiceProto.PublishRequest.newBuilder();
//
//            ModelServiceProto.PublishResponse response = null;
//
//            if (callName.equals(Dict.LIST_ALL_MODEL)) {
//                response = blockingStub.listAllModel(builder.build());
//            } else if (callName.equals(Dict.GET_MODEL_BY_NAME_AND_NAMESPACE)) {
//                String tableName = String.valueOf(body.get(Dict.TABLE_NAME));
//                String namespace = String.valueOf(body.get(Dict.NAMESPACE));
//
//                Preconditions.checkArgument(StringUtils.isNotBlank(tableName), "parameter tableName is blank");
//                Preconditions.checkArgument(StringUtils.isNotBlank(namespace), "parameter namespace is blank");
//
//                builder.setTableName(tableName).setNamespace(namespace);
//                response = blockingStub.getModelByTableNameAndNamespace(builder.build());
//            } else if (callName.equals(Dict.GET_MODEL_BY_SERVICE_ID)) {
//                String serviceId = String.valueOf(body.get(Dict.SERVICE_ID));
//
//                Preconditions.checkArgument(StringUtils.isNotBlank(serviceId), "parameter serviceId is blank");
//
//                builder.setServiceId(serviceId);
//                response = blockingStub.getModelByServiceId(builder.build());
//            }
//
//            if (response != null) {
//                if (logger.isDebugEnabled()) {
//                    logger.debug("response: {}", response);
//                }
//
//                return ReturnResult.success(response.getStatusCode(), response.getMessage(), JSONObject.parse(response.getData().toStringUtf8()));
//            } else {
//                throw new RemoteRpcException(StatusCode.UNAVAILABLE_REQUEST, "unavailable request: " + callName);
//            }
//        };
//    }


//    @GetMapping("/model/listAllModel")
//    public ReturnResult listAllModel(String host, int port) throws Exception {
//        if (logger.isDebugEnabled()) {
//            logger.debug("list all model request, host: {}, port: {}", host, port);
//        }
//
//        ModelServiceGrpc.ModelServiceBlockingStub blockingStub = getModelServiceBlockingStub(host, port);
//
//        ModelServiceProto.QueryModelRequest.Builder  builder =   ModelServiceProto.QueryModelRequest.newBuilder();
//
//        ModelServiceProto.QueryModelResponse response = blockingStub.queryModel(builder.build());
//
//        if (logger.isDebugEnabled()) {
//            logger.debug("response: {}", response);
//        }
//
//
//        return ReturnResult.build(response.getStatusCode(), response.getMessage(), JSONObject.parse(response.getData().toStringUtf8()));
//    }

//    @GetMapping("/model/getModelByNameAndNamespace")
//    public ReturnResult getModelByNameAndNamespace(String host, int port, String tableName, String namespace) throws Exception {
//        Preconditions.checkArgument(StringUtils.isNotBlank(tableName), "parameter tableName is blank");
//        Preconditions.checkArgument(StringUtils.isNotBlank(namespace), "parameter namespace is blank");
//
//        if (logger.isDebugEnabled()) {
//            logger.debug("get model by tableName and namespace, host: {}, port: {}, tableName: {}, namespace: {}", host, port, tableName, namespace);
//        }
//
//        ModelServiceGrpc.ModelServiceBlockingStub blockingStub = getModelServiceBlockingStub(host, port);
//
//        ModelServiceProto.PublishRequest publishRequest = ModelServiceProto.PublishRequest.newBuilder()
//                .setTableName(tableName)
//                .setNamespace(namespace)
//                .build();
//
//        ModelServiceProto.PublishResponse response = blockingStub.queryModel(publishRequest);
//
//        if (logger.isDebugEnabled()) {
//            logger.debug("response: {}", response);
//        }
//
//        return ReturnResult.success(response.getStatusCode(), response.getMessage(), JSONObject.parse(response.getData().toStringUtf8()));
//    }
    @GetMapping("/model/query")
    public ReturnResult queryModel(String host, int port, String serviceId) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != 0, "parameter port is blank");

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
        if (modelInfosList != null) {
            for (ModelServiceProto.ModelInfoEx modelInfoEx : modelInfosList) {
                rows.add(JSONObject.parseObject(modelInfoEx.getContent()));
            }
        }

        data.put("total", rows.size());
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

            JSONObject data = JSON.parseObject(requestData);
            Preconditions.checkArgument(data.get(Dict.PARAMS_INITIATOR) != null, "parameter initiator not exist");
            Preconditions.checkArgument(data.get(Dict.PARAMS_ROLE) != null, "parameter role not exist");
            Preconditions.checkArgument(data.get(Dict.PARAMS_JOB_PARAMETERS) != null, "parameter job_parameters not exist");

            String resp = HttpClientPool.post(loadUrl, data, null);

            logger.info("publishLoad response : {}", resp);

            if (StringUtils.isNotBlank(resp)) {
                result.setRetcode(StatusCode.SUCCESS);
                result.setData(JSONObject.parseObject(resp));
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

            JSONObject data = JSON.parseObject(requestData);
            Preconditions.checkArgument(data.get(Dict.PARAMS_INITIATOR) != null, "parameter initiator not exist");
            Preconditions.checkArgument(data.get(Dict.PARAMS_ROLE) != null, "parameter role not exist");
            Preconditions.checkArgument(data.get(Dict.PARAMS_JOB_PARAMETERS) != null, "parameter job_parameters not exist");
            Preconditions.checkArgument(data.get(Dict.PARAMS_SERVICE_ID) != null, "parameter service_id not exist");

            String resp = HttpClientPool.post(bindUrl, data);

            logger.info("publishBind response : {}", resp);

            if (StringUtils.isNotBlank(resp)) {
                result.setRetcode(StatusCode.SUCCESS);
                result.setData(JSONObject.parseObject(resp));
            } else {
                result.setRetcode(StatusCode.GUEST_BIND_MODEL_ERROR);
                result.setRetmsg("publishBind failed");
            }
            return result;
        };
    }

    @PostMapping("/model/unload")
    public Callable<ReturnResult> unload(String host, int port, String tableName, String namespace) throws Exception {
        return () -> {
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
    public Callable<ReturnResult> unbind(String host, int port, String tableName, String namespace, String serviceId) throws Exception {
        return () -> {
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

    private ModelServiceGrpc.ModelServiceBlockingStub getModelServiceBlockingStub(String host, int port) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != 0, "parameter port was wrong");

        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        ModelServiceGrpc.ModelServiceBlockingStub blockingStub = ModelServiceGrpc.newBlockingStub(managedChannel);
        blockingStub = blockingStub.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS);
        return blockingStub;
    }

    private ModelServiceGrpc.ModelServiceFutureStub getModelServiceFutureStub(String host, int port) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != 0, "parameter port was wrong");

        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        ModelServiceGrpc.ModelServiceFutureStub futureStub = ModelServiceGrpc.newFutureStub(managedChannel);
        return futureStub;
    }

}
