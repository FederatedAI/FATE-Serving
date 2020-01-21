package com.webank.ai.fate.serving.core.rpc.core;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.exceptions.ErrorCode;
import com.webank.ai.fate.serving.core.exceptions.ShowDownRejectException;


import io.grpc.stub.AbstractStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;



/**
 * @Description 默认的服务适配器
 * @Author
 **/

public abstract class AbstractServiceAdaptor<req,resp> implements ServiceAdaptor<req,resp> {

    Logger flowLogger = LoggerFactory.getLogger( "flow");

    Logger logger =  LoggerFactory.getLogger( this.getClass().getName());


    public AbstractServiceAdaptor(){


    }


    public  void addPreProcessor(Interceptor interceptor){

        preChain.addInterceptor(interceptor);
    };

    public  void addPostProcessor(Interceptor interceptor){
        postChain.addInterceptor(interceptor);
    };

    static public AtomicInteger requestInHandle =  new AtomicInteger(0);

    public static boolean  isOpen=true;

    public ServiceAdaptor getServiceAdaptor() {
        return serviceAdaptor;
    }

    public void setServiceAdaptor(ServiceAdaptor serviceAdaptor) {
        this.serviceAdaptor = serviceAdaptor;
    }


    ServiceAdaptor serviceAdaptor;

    InterceptorChain preChain = new DefaultInterceptorChain();

    InterceptorChain postChain = new DefaultInterceptorChain();

    public AbstractStub getServiceStub() {
        return serviceStub;
    }

    public void setServiceStub(AbstractStub serviceStub) {
        this.serviceStub = serviceStub;
    }

    private AbstractStub serviceStub;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    String serviceName;

    public abstract resp doService(Context context, InboundPackage<req> data, OutboundPackage<resp>  outboundPackage)  ;

    /**
     * @param context
     * @param data
     * @return
     * @throws Exception
     */
    @Override
    public  OutboundPackage<resp> service(Context context , InboundPackage<req> data) throws Exception {

        OutboundPackage<resp>    outboundPackage= new OutboundPackage<resp>();
        long begin = System.currentTimeMillis();

        List<Throwable> exceptions = Lists.newArrayList();
        context.setReturnCode("0");
        if(!isOpen){
            return  this.serviceFailInner(context,data,new ShowDownRejectException());
        }

        try {
            requestInHandle.addAndGet(1);


            resp result=null;
            context.setServiceName(this.serviceName);

            preChain.doPreProcess(context,data,outboundPackage);

            try {
                result = doService(context, data, outboundPackage);
                if(logger.isDebugEnabled()) {
                    logger.debug("do service, router info: {}, service name: {}, result: {}", JSON.toJSONString(data.getRouterInfo()), serviceName, result);
                }
            }catch(Throwable e){
                /**
                 * 这里catch的原因是，就算发生异常也要走完后处理
                 */
                e.printStackTrace();
                exceptions.add(e);
                logger.error("do service fail, cause by: {}", e.getMessage());
            }
            outboundPackage.setData(result);
            postChain.doPostProcess(context,data,outboundPackage);

        }
        catch (Throwable e) {
            exceptions.add(e);
            logger.info(e.getMessage());
        } finally {

            requestInHandle.decrementAndGet();
            long end = System.currentTimeMillis();
            long cost = end - begin;
            try {
                logger.info("kaideng test");
                flowLogger.info("{}|{}|{}|{}|" +
                                "{}|{}|{}|{}|" +
                                "{}|{}",
                        begin, context.getSourceIp(), context.getCaseId(), context.getGuestAppId(),
                        context.getHostAppid(), context.getReturnCode(), end - begin,
                        context.getDownstreamCost(), serviceName, context.getRouterInfo() != null ? context.getRouterInfo() : "NO_ROUTER_INFO");
            }catch(Exception e){
                logger.error("print flow log error",e);
            }

            if(exceptions.size()!=0){
                try {
                    outboundPackage = this.serviceFail(context, data, exceptions);
                }catch(Throwable e){
                    logger.error("handle serviceFail error",e);
                }
            }
        }
        return outboundPackage;

    }

    protected  OutboundPackage<resp>  serviceFailInner(Context context, InboundPackage<req> data, Throwable e) throws Exception{

        Map result = new HashMap();
        OutboundPackage<resp> outboundPackage = new OutboundPackage<resp>();
        result.put(Dict.MESSAGE, e.getMessage());
        ErrorMessageUtil.handleException(result,e);
        context.setReturnCode(result.get(Dict.CODE)!=null?result.get(Dict.CODE).toString(): ErrorCode.SYSTEM_ERROR.toString());
        resp  rsp = transformErrorMap(context ,result);
        outboundPackage.setData(rsp);
        return  outboundPackage;
    }


    @Override
    public OutboundPackage<resp> serviceFail(Context context, InboundPackage<req> data, List<Throwable> errors) throws Exception {

        logger.error("serviceFail {}", errors);
        Throwable e = errors.get(0);
        return  serviceFailInner(context,data,e);

    }

    abstract  protected  resp  transformErrorMap(Context context,Map  data);

    private String objectToJson(Object obj) {
        return JSONObject.toJSONString(obj, SerializerFeature.WriteEnumUsingToString);
    }







}