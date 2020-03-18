package com.webank.ai.fate.serving.guest.provider;

import com.webank.ai.fate.serving.core.bean.BatchInferenceResult;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.InferenceRetCode;
import com.webank.ai.fate.serving.core.exceptions.BaseException;
import com.webank.ai.fate.serving.core.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;

import java.util.List;

public abstract  class AbstractServingServiceProvider<req,resp>   extends AbstractServiceAdaptor<req,resp>{


//    public class InferenceRetCode {
//        public static final int OK = 0;
//        public static final int EMPTY_DATA = 100;
//        public static final int NUMERICAL_ERROR = 101;
//        public static final int INVALID_FEATURE = 102;
//        public static final int GET_FEATURE_FAILED = 103;
//        public static final int LOAD_MODEL_FAILED = 104;
//        public static final int NETWORK_ERROR = 105;
//        public static final int DISK_ERROR = 106;
//        public static final int STORAGE_ERROR = 107;
//        public static final int COMPUTE_ERROR = 108;
//        public static final int NO_RESULT = 109;
//        public static final int SYSTEM_ERROR = 110;
//        public static final int ADAPTER_ERROR = 111;
//        public static final int DEAL_FEATURE_FAILED = 112;
//        public static final int NO_FEATURE = 113;
//    }

//
//    @Override
//    public OutboundPackage<resp> serviceFail(Context context, InboundPackage<req> data, List<Throwable> errors) throws Exception {
//
//        Throwable e = errors.get(0);
//        if(resp ReturnResult ) {
//            ReturnResult returnResult = new ReturnResult();
//        }
//        else if(resp instanceof BatchInferenceResult) {
//
//            }
//        if(e instanceof BaseException){
//            BaseException  error = (BaseException)e;
//            returnResult.setRetcode(error.getRetcode());
//            returnResult.setRetmsg(error.getMessage());
//        }else {
//            returnResult.setRetmsg();
//            returnResult.setRetcode(InferenceRetCode.SYSTEM_ERROR);
//        }
//
//        return  serviceFailInner(context,data,e);
//
//    }
}
