package com.webank.ai.fate.serving.core.rpc.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.core.async.AsyncMessageEvent;
import com.webank.ai.fate.serving.core.bean.BatchHostFederatedParams;
import com.webank.ai.fate.serving.core.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.ShowDownRejectException;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.utils.DisruptorUtil;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import io.grpc.stub.AbstractStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @Description 默认的服务适配器
 * @Author
 **/

public abstract class AbstractServiceAdaptor<req, resp> implements ServiceAdaptor<req, resp> {

    static public AtomicInteger requestInHandle = new AtomicInteger(0);
    public static boolean isOpen = true;
    protected Logger flowLogger = LoggerFactory.getLogger("flow");
    protected String serviceName;
    Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    ServiceAdaptor serviceAdaptor;
    InterceptorChain preChain = new DefaultInterceptorChain();

    ;
    InterceptorChain postChain = new DefaultInterceptorChain();

    ;
    private Map<String, Method> methodMap = Maps.newHashMap();
    private AbstractStub serviceStub;

    public AbstractServiceAdaptor() {

    }

    public Map<String, Method> getMethodMap() {
        return methodMap;
    }

    public void setMethodMap(Map<String, Method> methodMap) {
        this.methodMap = methodMap;
    }

    public void addPreProcessor(Interceptor interceptor) {

        preChain.addInterceptor(interceptor);
    }

    public void addPostProcessor(Interceptor interceptor) {
        postChain.addInterceptor(interceptor);
    }

    public ServiceAdaptor getServiceAdaptor() {
        return serviceAdaptor;
    }

    public void setServiceAdaptor(ServiceAdaptor serviceAdaptor) {
        this.serviceAdaptor = serviceAdaptor;
    }

    public AbstractStub getServiceStub() {
        return serviceStub;
    }

    public void setServiceStub(AbstractStub serviceStub) {
        this.serviceStub = serviceStub;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    protected abstract resp doService(Context context, InboundPackage<req> data, OutboundPackage<resp> outboundPackage);

    /**
     * @param context
     * @param data
     * @return
     * @throws Exception
     */
    @Override
    public OutboundPackage<resp> service(Context context, InboundPackage<req> data) throws RuntimeException {

        OutboundPackage<resp> outboundPackage = new OutboundPackage<resp>();
        long begin = System.currentTimeMillis();
        context.preProcess();
        List<Throwable> exceptions = Lists.newArrayList();
        context.setReturnCode(StatusCode.SUCCESS);
        if (!isOpen) {
            return this.serviceFailInner(context, data, new ShowDownRejectException());
        }
        try {
            requestInHandle.addAndGet(1);

            resp result = null;

            context.setServiceName(this.serviceName);


            try {
                preChain.doPreProcess(context, data, outboundPackage);
                result = doService(context, data, outboundPackage);
                if (logger.isDebugEnabled()) {
                    logger.debug("do service, router info: {}, service name: {}, result: {}", JsonUtil.object2Json(data.getRouterInfo()), serviceName, result);
                }
            } catch (Throwable e) {
                exceptions.add(e);
                logger.error("do service fail, cause by: {}", e.getMessage());
            }
            outboundPackage.setData(result);
            postChain.doPostProcess(context, data, outboundPackage);

        } catch (Throwable e) {
            exceptions.add(e);
            logger.error(e.getMessage());
        } finally {
            requestInHandle.decrementAndGet();
            long end = System.currentTimeMillis();
            long cost = end - begin;

            if (exceptions.size() != 0) {
                try {
                    outboundPackage = this.serviceFail(context, data, exceptions);
                    AsyncMessageEvent messageEvent = new AsyncMessageEvent();
                    messageEvent.setName(Dict.EVENT_ERROR);
                    messageEvent.setTimestamp(end);
                    messageEvent.setAction(context.getActionType());
                    messageEvent.setData("");
                    DisruptorUtil.producer(messageEvent);
                } catch (Throwable e) {
                    logger.error("error ", e);
                }
            }
            printFlowLog(context);
        }
        return outboundPackage;
    }

    protected void printFlowLog(Context context) {

        flowLogger.info("{}|{}|{}|{}|" +
                        "{}|{}|{}|{}|" +
                        "{}|{}",
                context.getSourceIp(), context.getCaseId(), context.getGuestAppId(),
                context.getHostAppid(), context.getReturnCode(), context.getCostTime(),
                context.getDownstreamCost(), serviceName, context.getRouterInfo() != null ? context.getRouterInfo() : "NO_ROUTER_INFO");
    }


    protected OutboundPackage<resp> serviceFailInner(Context context, InboundPackage<req> data, Throwable e) {

        Map result = new HashMap();
        OutboundPackage<resp> outboundPackage = new OutboundPackage<resp>();
       // result.put(Dict.MESSAGE, e.getMessage());
        result= ErrorMessageUtil.handleExceptionToMap(e);
        context.setReturnCode(result.get(Dict.RET_CODE) != null ? result.get(Dict.RET_CODE).toString() : StatusCode.SYSTEM_ERROR);
        resp rsp = transformErrorMap(context, result);
        outboundPackage.setData(rsp);
        return outboundPackage;
    }


    @Override
    public OutboundPackage<resp> serviceFail(Context context, InboundPackage<req> data, List<Throwable> errors) throws RuntimeException {


        Throwable e = errors.get(0);
        logger.error("service fail ", e);
        return serviceFailInner(context, data, e);

    }


    abstract protected resp transformErrorMap(Context context, Map data);

    private String objectToJson(Object obj) {
        return JsonUtil.object2Json(obj);
    }


    /**
     * 需要支持多方host
     *
     * @param context
     * @param batchInferenceRequest
     * @return
     */
    protected BatchHostFederatedParams buildBatchHostFederatedParams(Context context, BatchInferenceRequest batchInferenceRequest, Model guestModel, Model hostModel) {

        BatchHostFederatedParams batchHostFederatedParams = new BatchHostFederatedParams();
        String seqNo = batchInferenceRequest.getSeqno();
        batchHostFederatedParams.setGuestPartyId(guestModel.getPartId());
        batchHostFederatedParams.setHostPartyId(hostModel.getPartId());
        List<BatchHostFederatedParams.SingleInferenceData> sendToHostDataList = Lists.newArrayList();
        List<BatchInferenceRequest.SingleInferenceData> guestDataList = batchInferenceRequest.getBatchDataList();
        for (BatchInferenceRequest.SingleInferenceData singleInferenceData : guestDataList) {
            BatchHostFederatedParams.SingleInferenceData singleBatchHostFederatedParam = new BatchHostFederatedParams.SingleInferenceData();
            singleBatchHostFederatedParam.setSendToRemoteFeatureData(singleInferenceData.getSendToRemoteFeatureData());
            singleBatchHostFederatedParam.setIndex(singleInferenceData.getIndex());
            sendToHostDataList.add(singleBatchHostFederatedParam);
        }
        batchHostFederatedParams.setBatchDataList(sendToHostDataList);
        batchHostFederatedParams.setHostTableName(hostModel.getTableName());
        batchHostFederatedParams.setHostNamespace(hostModel.getNamespace());
        batchHostFederatedParams.setCaseId(batchInferenceRequest.getCaseId());
        return batchHostFederatedParams;

    }


}