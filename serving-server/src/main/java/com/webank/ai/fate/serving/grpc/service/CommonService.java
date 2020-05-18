package com.webank.ai.fate.serving.grpc.service;

import com.codahale.metrics.MetricRegistry;
import com.webank.ai.fate.api.networking.common.CommonServiceGrpc;
import com.webank.ai.fate.api.networking.common.CommonServiceProto;
import com.webank.ai.fate.register.annotions.RegisterService;
import com.webank.ai.fate.serving.common.provider.CommonServiceProvider;
import com.webank.ai.fate.serving.core.bean.CommonActionType;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.ServingServerContext;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class CommonService  extends CommonServiceGrpc.CommonServiceImplBase{

    private static final Logger logger = LoggerFactory.getLogger(CommonService.class);
    private static final String QUERY_METRICS = "queryMetrics";
    private static final String UPDATE_FLOW_RULE = "updateFlowRule";
    private static final String LIST_PROPS = "listProps";

    @Autowired
    CommonServiceProvider commonServiceProvider;

    @Autowired
    MetricRegistry metricRegistry;

    @Autowired
    Environment environment;

    @Override
    @RegisterService(serviceName = QUERY_METRICS)
    public void queryMetrics(CommonServiceProto.QueryMetricRequest request, StreamObserver<CommonServiceProto.CommonResponse> responseObserver) {
        Context context = prepareContext(QUERY_METRICS);
        context.setActionType(CommonActionType.QUERY_METRICS.name());
        InboundPackage inboundPackage = new InboundPackage();
        inboundPackage.setBody(request);
        OutboundPackage outboundPackage = commonServiceProvider.service(context, inboundPackage);
        CommonServiceProto.CommonResponse response = (CommonServiceProto.CommonResponse) outboundPackage.getData();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    @RegisterService(serviceName = UPDATE_FLOW_RULE)
    public void updateFlowRule(CommonServiceProto.UpdateFlowRuleRequest request, StreamObserver<CommonServiceProto.CommonResponse> responseObserver) {
        Context context = prepareContext(UPDATE_FLOW_RULE);
        context.setActionType(CommonActionType.UPDATE_FLOW_RULE.name());
        InboundPackage inboundPackage = new InboundPackage();
        inboundPackage.setBody(request);
        OutboundPackage outboundPackage = commonServiceProvider.service(context, inboundPackage);
        CommonServiceProto.CommonResponse response = (CommonServiceProto.CommonResponse) outboundPackage.getData();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    @RegisterService(serviceName = LIST_PROPS)
    public void listProps(CommonServiceProto.QueryPropsRequest request, StreamObserver<CommonServiceProto.CommonResponse> responseObserver) {
        Context context = prepareContext(LIST_PROPS);
        context.setActionType(CommonActionType.LIST_PROPS.name());
        InboundPackage inboundPackage = new InboundPackage();
        inboundPackage.setBody(request);
        OutboundPackage outboundPackage = commonServiceProvider.service(context, inboundPackage);
        CommonServiceProto.CommonResponse response = (CommonServiceProto.CommonResponse) outboundPackage.getData();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Context prepareContext(String interfaceName) {
        ServingServerContext context = new ServingServerContext();
        context.setMetricRegistry(this.metricRegistry);
        context.setEnvironment(environment);
        context.setInterfaceName(interfaceName);
        return context;
    }

}
