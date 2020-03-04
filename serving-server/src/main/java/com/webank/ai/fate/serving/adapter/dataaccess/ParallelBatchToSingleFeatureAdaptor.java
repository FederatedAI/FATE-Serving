package com.webank.ai.fate.serving.adapter.dataaccess;


import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.webank.ai.fate.serving.core.bean.BatchHostFeatureAdaptorResult;
import com.webank.ai.fate.serving.core.bean.BatchHostFederatedParams;
import com.webank.ai.fate.serving.core.bean.Context;

import java.util.List;
import java.util.concurrent.*;

public class ParallelBatchToSingleFeatureAdaptor  implements BatchFeatureDataAdaptor{


    int   timeout;

    SingleFeatureDataAdaptor  singleFeatureDataAdaptor;

    ListeningExecutorService listeningExecutorService= MoreExecutors.listeningDecorator(null);

    public  ParallelBatchToSingleFeatureAdaptor(int  core,  int max){
        new ThreadPoolExecutor(core ,max ,1000,TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(1000),new ThreadPoolExecutor.AbortPolicy());
    }




//    ThreadPoolExecutor(int corePoolSize,
//                       int maximumPoolSize,
//                       long keepAliveTime,
//                       TimeUnit unit,
//                       BlockingQueue<Runnable> workQueue,
//                       RejectedExecutionHandler handler)



    @Override
    public BatchHostFeatureAdaptorResult getFeatures(Context context, List<BatchHostFederatedParams.SingleInferenceData> featureIdList) {

        BatchHostFeatureAdaptorResult   result =   new  BatchHostFeatureAdaptorResult();

        CountDownLatch  countDownLatch = new CountDownLatch(featureIdList.size());

        for  (int i =0;i<featureIdList.size();i++){
           // featureIdList

            this.listeningExecutorService.submit(new Runnable() {
                @Override
                public void run() {

                    try{



                    }finally {
                        countDownLatch.countDown();
                    }

                }
            });
        }

        try {
            countDownLatch.await(timeout,TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /**
         *   如果等待超时也需要把已经返回的查询结果返回
         */








        return null;
    }

    @Override
    public List<ParamDescriptor> desc() {
        return null;
    }
}
