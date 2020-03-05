package com.webank.ai.fate.serving.interceptor;


import com.google.common.base.Preconditions;
import com.webank.ai.fate.register.utils.StringUtils;
import com.webank.ai.fate.serving.adapter.dataaccess.AdaptorDescriptor;
import com.webank.ai.fate.serving.adapter.dataaccess.BatchFeatureDataAdaptor;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.InferenceRetCode;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.Interceptor;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.utils.InferenceUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
@Service
public class HostBatchFeatureAdaptorInterceptor extends  AbstractInterceptor implements InitializingBean,AdaptorDescriptor {

    /**
     *  需要区分两种情况 ，host端提供批量接口 或者是单笔接口,若无批量接口但是有单笔接口
     *  则生成一个默认的批量Adaptor,将批量查询拆分成单笔查询并合并结果
     */


    BatchFeatureDataAdaptor   batchFeatureDataAdaptor;

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception{

        if(batchFeatureDataAdaptor==null){
            throw  new  RuntimeException();
        }
        BatchHostFederatedParams   batchHostFederatedParams = (BatchHostFederatedParams)inboundPackage.getBody();
        Preconditions.checkArgument(batchHostFederatedParams!=null);
        List<BatchHostFederatedParams.SingleInferenceData>   dataList = batchHostFederatedParams.getDataList();
        BatchHostFeatureAdaptorResult   batchHostFeatureAdaptorResult = batchFeatureDataAdaptor.getFeatures(context,dataList);
        /**
         *   将特征设回请求中
         */

        if(batchHostFeatureAdaptorResult==null){

            throw  new  RuntimeException();
        }

//        Map<Integer, BatchHostFeatureAdaptorResult.SingleBatchHostFeatureAdaptorResult>  batchHostFeatureAdaptorResult.();






        context.putData(Dict.FEATURE_DATA,batchHostFeatureAdaptorResult);

    };


    private void   mergeLocalFeatureToRequest(BatchHostFederatedParams  batchHostFederatedParams,BatchHostFeatureAdaptorResult  BatchHostFeatureAdaptorResult){

        List<BatchInferenceRequest.SingleInferenceData>  reqDataList =  batchHostFederatedParams.getDataList();



    }

//
//    private  ReturnResult getFeatureData(Context context, Map<String, Object> featureIds) {
//        ReturnResult defaultReturnResult = new ReturnResult();
//        String classPath = FeatureData.class.getPackage().getName() + "." + Configuration.getProperty(Dict.PROPERTY_ONLINE_DATA_ACCESS_ADAPTER);
//        FeatureData featureData = (FeatureData) InferenceUtils.getClassByName(classPath);
//        if (featureData == null) {
//            defaultReturnResult.setRetcode(InferenceRetCode.ADAPTER_ERROR);
//            return defaultReturnResult;
//        }
//        try {
//            return featureData.getData(context,featureIds);
//        } catch (Exception ex) {
//            defaultReturnResult.setRetcode(InferenceRetCode.GET_FEATURE_FAILED);
//            logger.error("get feature data error:", ex);
//            return defaultReturnResult;
//        }
//    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment =  environment;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        String  adaptorClass = environment.getProperty(Dict.PROPERTY_ONLINE_DATA_BATCH_ACCESS_ADAPTER,"").toString();
        if(StringUtils.isNotEmpty(adaptorClass)) {
            batchFeatureDataAdaptor =    (BatchFeatureDataAdaptor) InferenceUtils.getClassByName(adaptorClass);
        }

    }

    @Override
    public List<ParamDescriptor> desc() {

        if(this.batchFeatureDataAdaptor!=null)
        {
            return this.batchFeatureDataAdaptor.desc();
        }
        else{
            return null;
        }

    }
}
