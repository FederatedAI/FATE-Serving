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

package com.webank.ai.fate.networking.proxy.grpc.client;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.webank.ai.fate.api.core.BasicMeta;
import com.webank.ai.fate.api.networking.proxy.DataTransferServiceGrpc;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.networking.proxy.factory.GrpcStreamObserverFactory;
import com.webank.ai.fate.networking.proxy.factory.GrpcStubFactory;
import com.webank.ai.fate.networking.proxy.infra.Pipe;
import com.webank.ai.fate.networking.proxy.infra.ResultCallback;
import com.webank.ai.fate.networking.proxy.infra.impl.PacketQueueSingleResultPipe;
import com.webank.ai.fate.networking.proxy.infra.impl.SingleResultCallback;
import com.webank.ai.fate.networking.proxy.model.ServerConf;
import com.webank.ai.fate.networking.proxy.service.ConfFileBasedFdnRouter;
import com.webank.ai.fate.networking.proxy.service.FdnRouter;
import com.webank.ai.fate.networking.proxy.util.AuthUtils;
import com.webank.ai.fate.networking.proxy.util.ErrorUtils;
import com.webank.ai.fate.networking.proxy.util.ToStringUtils;
import com.webank.ai.fate.register.common.Constants;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.url.CollectionUtils;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.serving.core.bean.EncryptMethod;
import com.webank.ai.fate.serving.core.bean.HostFederatedParams;
import com.webank.ai.fate.serving.core.bean.ModelInfo;
import com.webank.ai.fate.serving.core.utils.EncryptUtils;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
public class DataTransferPipedClient {
    private static final Logger logger = LoggerFactory.getLogger(DataTransferPipedClient.class);
    @Autowired
    private GrpcStubFactory grpcStubFactory;
    @Autowired
    private GrpcStreamObserverFactory grpcStreamObserverFactory;
    @Autowired
    private ServerConf serverConf;
    @Autowired
    private FdnRouter fdnRouter;
    @Autowired
    private ToStringUtils toStringUtils;
    @Autowired
    private ErrorUtils errorUtils;
    @Autowired
    private AuthUtils authUtils;

    public static  RouterService routerService;


    private BasicMeta.Endpoint endpoint;
    private boolean needSecureChannel;
    private static long MAX_AWAIT_HOURS = 24;
    private static String SERVICE_ROLE_NAME = "serving-1.0";

    public DataTransferPipedClient() {
        needSecureChannel = false;
    }

    public void push(Proxy.Metadata metadata, Pipe pipe) {
        String onelineStringMetadata = toStringUtils.toOneLineString(metadata);
        logger.info("[PUSH][CLIENT] client send push to server: {}",
                onelineStringMetadata);
        DataTransferServiceGrpc.DataTransferServiceStub stub = getStub(metadata.getSrc(), metadata.getDst());

        try {
            Proxy.Topic from = metadata.getSrc();
            Proxy.Topic to = metadata.getDst();
            stub = getStub(from, to);
        } catch (Exception e) {
            logger.error("[PUSH][CLIENT] error when creating push stub");
            pipe.onError(e);
        }

        final CountDownLatch finishLatch = new CountDownLatch(1);
        final ResultCallback<Proxy.Metadata> resultCallback = new SingleResultCallback<Proxy.Metadata>();

        StreamObserver<Proxy.Metadata> responseObserver =
                grpcStreamObserverFactory.createClientPushResponseStreamObserver(resultCallback, finishLatch);

        StreamObserver<Proxy.Packet> requestObserver = stub.push(responseObserver);
        logger.info("[PUSH][CLIENT] push stub: {}, metadata: {}",
                stub.getChannel(), onelineStringMetadata);

        int emptyRetryCount = 0;
        int maxRetryCount = 60;
        Proxy.Packet packet = null;
        do {
            packet = (Proxy.Packet) pipe.read(1, TimeUnit.SECONDS);

            if (packet != null) {
                requestObserver.onNext(packet);
                emptyRetryCount = 0;
            } else {
                ++emptyRetryCount;
                if (emptyRetryCount % maxRetryCount == 0) {
                    logger.info("[PUSH][CLIENT] push stub waiting. empty retry count: {}, metadata: {}",
                            emptyRetryCount, onelineStringMetadata);
                }
            }
        } while ((packet != null || !pipe.isDrained()) && emptyRetryCount < 30 && !pipe.hasError());

        logger.info("[PUSH][CLIENT] break out from loop. Proxy.Packet is null? {} ; pipe.isDrained()? {}" +
                        ", pipe.hasError? {}, metadata: {}",
                packet == null, pipe.isDrained(), pipe.hasError(), onelineStringMetadata);

        if (pipe.hasError()) {
            Throwable error = pipe.getError();
            logger.error("[PUSH][CLIENT] push error: {}, metadata: {}",
                    ExceptionUtils.getStackTrace(error), onelineStringMetadata);
            requestObserver.onError(error);

            return;
        }

        requestObserver.onCompleted();
        try {
            finishLatch.await(MAX_AWAIT_HOURS, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            logger.error("[PUSH][CLIENT] client push: finishLatch.await() interrupted");
            requestObserver.onError(errorUtils.toGrpcRuntimeException(e));
            pipe.onError(e);
            Thread.currentThread().interrupt();
            return;
        }

        if (pipe instanceof PacketQueueSingleResultPipe) {
            PacketQueueSingleResultPipe convertedPipe = (PacketQueueSingleResultPipe) pipe;
            if (resultCallback.hasResult()) {
                convertedPipe.setResult(resultCallback.getResult());
            } else {
                logger.warn("No Proxy.Metadata returned in pipe. request metadata: {}",
                        onelineStringMetadata);
            }
        }
        pipe.onComplete();

        logger.info("[PUSH][CLIENT] push closing pipe. metadata: {}",
                onelineStringMetadata);
    }

    public void pull(Proxy.Metadata metadata, Pipe pipe) {
        String onelineStringMetadata = toStringUtils.toOneLineString(metadata);
        logger.info("[PULL][CLIENT] client send pull to server: {}", onelineStringMetadata);
        DataTransferServiceGrpc.DataTransferServiceStub stub = getStub(metadata.getDst(), metadata.getSrc());

        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<Proxy.Packet> responseObserver =
                grpcStreamObserverFactory.createClientPullResponseStreamObserver(pipe, finishLatch, metadata);

        stub.pull(metadata, responseObserver);
        logger.info("[PULL][CLIENT] pull stub: {}, metadata: {}",
                stub.getChannel(), onelineStringMetadata);

        try {
            finishLatch.await(MAX_AWAIT_HOURS, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            logger.error("[PULL][CLIENT] client pull: finishLatch.await() interrupted");
            responseObserver.onError(errorUtils.toGrpcRuntimeException(e));
            pipe.onError(e);
            Thread.currentThread().interrupt();
            return;
        }

        responseObserver.onCompleted();
    }

    public void unaryCall(Proxy.Packet packet, Pipe pipe) {
        Preconditions.checkNotNull(packet);

        Proxy.Metadata header = packet.getHeader();

        final CountDownLatch finishLatch = new CountDownLatch(1);
        StreamObserver<Proxy.Packet> responseObserver = grpcStreamObserverFactory
                .createClientUnaryCallResponseStreamObserver(pipe, finishLatch, header);

        try {
            String onelineStringMetadata = toStringUtils.toOneLineString(header);
            logger.info("[UNARYCALL][CLIENT] client send unary call to server: {}", onelineStringMetadata);

            packet = authUtils.addAuthInfo(packet);

            DataTransferServiceGrpc.DataTransferServiceStub stub = getStub(
                    packet.getHeader().getSrc(), packet.getHeader().getDst(), packet);

            stub.unaryCall(packet, responseObserver);

            logger.info("[UNARYCALL][CLIENT] unary call stub: {}, metadata: {}",
                    stub.getChannel(), onelineStringMetadata);

            try {
                finishLatch.await(MAX_AWAIT_HOURS, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                logger.error("[UNARYCALL][CLIENT] client unary call: finishLatch.await() interrupted");
                responseObserver.onError(errorUtils.toGrpcRuntimeException(e));
                pipe.onError(e);
                Thread.currentThread().interrupt();
                return;
            }
        } catch (Exception e) {
            logger.error("[UNARYCALL][CLIENT] client unary call: exception: ", e);
            responseObserver.onError(errorUtils.toGrpcRuntimeException(e));
            pipe.onError(e);
            Thread.currentThread().interrupt();
            return;
        }

        responseObserver.onCompleted();
    }

    private DataTransferServiceGrpc.DataTransferServiceStub getStub(Proxy.Topic from, Proxy.Topic to, Proxy.Packet pack
    ) {



        DataTransferServiceGrpc.DataTransferServiceStub stub = null;

        String useZkRouterString = serverConf.getProperties().getProperty("useZkRouter", "false");

        boolean useZkRouter = Boolean.valueOf(useZkRouterString);
        if (fdnRouter instanceof ConfFileBasedFdnRouter && useZkRouter) {

            ConfFileBasedFdnRouter confFileBasedFdnRouter = (ConfFileBasedFdnRouter) fdnRouter;
            Map<String, Map<String, List<BasicMeta.Endpoint>>> routerTable = confFileBasedFdnRouter.getRouteTable();

            if (routerTable.containsKey(to.getPartyId()) &&routerTable.get(to.getPartyId()).get(SERVICE_ROLE_NAME)!=null&& SERVICE_ROLE_NAME.equals(to.getRole())) {

                stub = routerByServiceRegister(from, to, pack);
                if (stub != null) {
                    logger.info("appid {} register return stub", to.getPartyId());
                    return stub;
                } else {
                    logger.info("appid {} register not return stub", to.getPartyId());
                    return null;
                }

            }
        }

        if (endpoint == null && !fdnRouter.isAllowed(from, to)) {
            throw new SecurityException("no permission from " + toStringUtils.toOneLineString(from)
                    + " to " + toStringUtils.toOneLineString(to));
        }


        if (endpoint == null) {
            stub = grpcStubFactory.getAsyncStub(to);
        } else {
            stub = grpcStubFactory.getAsyncStub(endpoint);
        }

        logger.info("[ROUTE] route info: {} routed to {}", toStringUtils.toOneLineString(to),
                toStringUtils.toOneLineString(fdnRouter.route(to)));

        fdnRouter.route(from);

        return stub;
    }


    private DataTransferServiceGrpc.DataTransferServiceStub getStub(Proxy.Topic from, Proxy.Topic to
    ) {

        DataTransferServiceGrpc.DataTransferServiceStub stub = null;

        if (endpoint == null && !fdnRouter.isAllowed(from, to)) {
            throw new SecurityException("no permission from " + toStringUtils.toOneLineString(from)
                    + " to " + toStringUtils.toOneLineString(to));
        }


        if (endpoint == null) {
            stub = grpcStubFactory.getAsyncStub(to);
        } else {
            stub = grpcStubFactory.getAsyncStub(endpoint);
        }

        logger.info("[ROUTE] route info: {} routed to {}", toStringUtils.toOneLineString(to),
                toStringUtils.toOneLineString(fdnRouter.route(to)));

        fdnRouter.route(from);

        return stub;
    }

    private static final String MODEL_KEY_SEPARATOR = "&";

    public static String genModelKey(String name, String namespace) {
        return StringUtils.join(Arrays.asList(name, namespace), MODEL_KEY_SEPARATOR);
    }


    private DataTransferServiceGrpc.DataTransferServiceStub routerByServiceRegister(Proxy.Topic from, Proxy.Topic to, Proxy.Packet pack) {
        DataTransferServiceGrpc.DataTransferServiceStub stub = null;
        String partId = to.getPartyId();
        String role = to.getRole();
        String name = to.getName();
        String serviceName = pack.getHeader().getCommand().getName();
        String version = pack.getHeader().getOperator();
        String data = pack.getBody().getValue().toStringUtf8();
        HostFederatedParams requestData = JSON.parseObject(data, HostFederatedParams.class);
        ModelInfo partnerModelInfo = requestData.getPartnerModelInfo();
       // (partnerModelInfo.getName(), partnerModelInfo.getNamespace()
        String key =genModelKey(partnerModelInfo.getName(), partnerModelInfo.getNamespace());
        String md5Key = EncryptUtils.encrypt(key, EncryptMethod.MD5);
        String urlString = "serving/" + md5Key + "/unaryCall";
        URL paramUrl = URL.valueOf(urlString);
        if(StringUtils.isNotEmpty(version)) {
            paramUrl= paramUrl.addParameter(Constants.VERSION_KEY,version
            );
        }

        List<URL> urls = routerService.router(paramUrl);
        logger.info("try to find {} returns {}",urlString,urls);
        if (CollectionUtils.isNotEmpty(urls)) {
            URL url = urls.get(0);
            BasicMeta.Endpoint.Builder builder = BasicMeta.Endpoint.newBuilder();
            builder.setIp(url.getHost());
            builder.setPort(url.getPort());
            BasicMeta.Endpoint endpoint = builder.build();
            stub = grpcStubFactory.getAsyncStub(endpoint);
        }
        return stub;
    }

    public boolean isNeedSecureChannel() {
        return needSecureChannel;
    }

    public void setNeedSecureChannel(boolean needSecureChannel) {
        this.needSecureChannel = needSecureChannel;
    }

    public BasicMeta.Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(BasicMeta.Endpoint endpoint) {
        this.endpoint = endpoint;
    }
}
