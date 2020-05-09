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

package com.webank.ai.fate.serving.adapter.dataaccess;


import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.webank.ai.fate.serving.core.adaptor.BatchFeatureDataAdaptor;
import com.webank.ai.fate.serving.core.adaptor.SingleFeatureDataAdaptor;
import com.webank.ai.fate.serving.core.bean.BatchHostFeatureAdaptorResult;
import com.webank.ai.fate.serving.core.bean.BatchHostFederatedParams;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.ReturnResult;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 许多host方并未提供批量查询接口，这个类将批量请求拆分成单笔请求发送，再合结果
 */
public class ParallelBatchToSingleFeatureAdaptor implements BatchFeatureDataAdaptor {

    int timeout;

    SingleFeatureDataAdaptor singleFeatureDataAdaptor;

    ListeningExecutorService listeningExecutorService = MoreExecutors.listeningDecorator(null);

    public ParallelBatchToSingleFeatureAdaptor(int core, int max) {
        new ThreadPoolExecutor(core, max, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1000), new ThreadPoolExecutor.AbortPolicy());
    }

    @Override
    public void init(Context context) {

    }

    @Override
    public BatchHostFeatureAdaptorResult getFeatures(Context context, List<BatchHostFederatedParams.SingleInferenceData> featureIdList) {

        BatchHostFeatureAdaptorResult result = new BatchHostFeatureAdaptorResult();
        CountDownLatch countDownLatch = new CountDownLatch(featureIdList.size());
        for (int i = 0; i < featureIdList.size(); i++) {

            BatchHostFederatedParams.SingleInferenceData singleInferenceData = featureIdList.get(i);
            // TODO: 2020/3/4    这里需要加 线程池满后的异常处理
            this.listeningExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        Integer index = singleInferenceData.getIndex();
                        ReturnResult returnResult = singleFeatureDataAdaptor.getData(context, singleInferenceData.getFeatureData());
                        BatchHostFeatureAdaptorResult.SingleBatchHostFeatureAdaptorResult adaptorResult = new BatchHostFeatureAdaptorResult.SingleBatchHostFeatureAdaptorResult();
                        adaptorResult.setFeatures(returnResult.getData());
                        result.getIndexResultMap().put(index, adaptorResult);
                    } finally {
                        countDownLatch.countDown();
                    }

                }
            });
        }
        /**
         *   这里的超时时间需要设置比rpc超时时间短，否则没有意义
         */
        try {
            countDownLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /**
         *   如果等待超时也需要把已经返回的查询结果返回
         */


        return result;

    }

    @Override
    public List<ParamDescriptor> desc() {
        return null;
    }
}
