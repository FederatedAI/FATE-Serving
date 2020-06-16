package com.webank.ai.fate.serving.proxy.rpc.grpc;

import com.webank.ai.fate.api.networking.common.CommonServiceGrpc;
import com.webank.ai.fate.api.networking.common.CommonServiceProto;
import com.webank.ai.fate.register.annotions.RegisterService;
import com.webank.ai.fate.serving.core.bean.BaseContext;
import com.webank.ai.fate.serving.core.bean.CommonActionType;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.proxy.rpc.provider.CommonServiceProvider;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CommonRequestHandler extends CommonServiceGrpc.CommonServiceImplBase{

//    private static final Logger logger = LoggerFactory.getLogger(CommonService.class);
    private static final String QUERY_METRICS = "queryMetrics";
    private static final String UPDATE_FLOW_RULE = "updateFlowRule";
    private static final String LIST_PROPS = "listProps";
    private static final String QUERY_JVM = "queryJvm";
    @Autowired
    CommonServiceProvider commonServiceProvider;

    @Override
    @RegisterService(serviceName = QUERY_METRICS)
    public void queryMetrics(CommonServiceProto.QueryMetricRequest request, StreamObserver<CommonServiceProto.CommonResponse> responseObserver) {
        Context context = prepareContext(CommonActionType.QUERY_METRICS.name());
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
        Context context = prepareContext(CommonActionType.UPDATE_FLOW_RULE.name());
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
        Context context = prepareContext(CommonActionType.LIST_PROPS.name());
        InboundPackage inboundPackage = new InboundPackage();
        inboundPackage.setBody(request);
        OutboundPackage outboundPackage = commonServiceProvider.service(context, inboundPackage);
        CommonServiceProto.CommonResponse response = (CommonServiceProto.CommonResponse) outboundPackage.getData();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    @RegisterService(serviceName = QUERY_JVM)
    public void queryJvmInfo(CommonServiceProto.QueryJvmInfoRequest request, StreamObserver<CommonServiceProto.CommonResponse> responseObserver) {
        Context context = prepareContext(CommonActionType.QUERY_JVM.name());
        InboundPackage inboundPackage = new InboundPackage();
        inboundPackage.setBody(request);
        OutboundPackage outboundPackage = commonServiceProvider.service(context, inboundPackage);
        CommonServiceProto.CommonResponse response = (CommonServiceProto.CommonResponse) outboundPackage.getData();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Context prepareContext(String actionType) {
        BaseContext context = new BaseContext();
        context.setActionType(actionType);
        context.setCaseId(UUID.randomUUID().toString().replaceAll("-", ""));
        return context;
    }

}
