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

package com.webank.ai.fate.serving.adaptor.dataaccess;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.webank.ai.fate.serving.core.adaptor.SingleFeatureDataAdaptor;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.utils.InferenceUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

/**
 * 许多host方并未提供批量查询接口，这个类将批量请求拆分成单笔请求发送，再合结果
 */
public class ParallelBatchToSingleFeatureAdaptor extends AbstractBatchFeatureDataAdaptor {

    private static final Logger logger = LoggerFactory.getLogger(HttpAdapter.class);

    int timeout;

    SingleFeatureDataAdaptor singleFeatureDataAdaptor;

    ListeningExecutorService listeningExecutorService;

    // 自定义Adapter初始化
    public ParallelBatchToSingleFeatureAdaptor(int core, int max, int timeout) {
        initExecutor(core, max, timeout);
    }

    // 默认Adapter初始化
    public ParallelBatchToSingleFeatureAdaptor() {

        // 默认线程池核心线程10
        int defaultCore = 10;

        // 默认线程池最大线程 100
        int defaultMax = 100;

        // 默认countDownLatch超时时间永远比grpc超时时间小
        timeout = MetaInfo.PROPERTY_GRPC_TIMEOUT.intValue() - 1;

        initExecutor(defaultCore, defaultMax, timeout);
    }


    private void initExecutor(int core, int max, int timeout) {
        SingleFeatureDataAdaptor singleFeatureDataAdaptor = null;
        String adaptorClass = MetaInfo.PROPERTY_FETTURE_BATCH_SINGLE_ADAPTOR;
        if (StringUtils.isNotEmpty(adaptorClass)) {
            logger.info("try to load single adaptor for ParallelBatchToSingleFeatureAdaptor {}", adaptorClass);
             singleFeatureDataAdaptor = (SingleFeatureDataAdaptor) InferenceUtils.getClassByName(adaptorClass);
        }

        if (singleFeatureDataAdaptor != null) {
            String implementationClass = singleFeatureDataAdaptor.getClass().getName();
            logger.info("SingleFeatureDataAdaptor implementation class: " + implementationClass);
        } else {
            logger.warn("SingleFeatureDataAdaptor is null.");
        }

        this.singleFeatureDataAdaptor = singleFeatureDataAdaptor;

        this.timeout = timeout;

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(core, max, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1000), new ThreadPoolExecutor.AbortPolicy());

        listeningExecutorService = MoreExecutors.listeningDecorator(threadPoolExecutor);
    }

    @Override
    public void init() {

    }

    @Override
    public BatchHostFeatureAdaptorResult getFeatures(Context context, List<BatchHostFederatedParams.SingleInferenceData> featureIdList) {
        BatchHostFeatureAdaptorResult result = new BatchHostFeatureAdaptorResult();
        CountDownLatch countDownLatch = new CountDownLatch(featureIdList.size());
        for (int i = 0; i < featureIdList.size(); i++) {
            BatchHostFederatedParams.SingleInferenceData singleInferenceData = featureIdList.get(i);
            try {
                this.listeningExecutorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Integer index = singleInferenceData.getIndex();
                            ReturnResult returnResult = singleFeatureDataAdaptor.getData(context, singleInferenceData.getSendToRemoteFeatureData());
                            BatchHostFeatureAdaptorResult.SingleBatchHostFeatureAdaptorResult adaptorResult = new BatchHostFeatureAdaptorResult.SingleBatchHostFeatureAdaptorResult();
                            adaptorResult.setFeatures(returnResult.getData());
                            result.getIndexResultMap().put(index, adaptorResult);
                        } finally {
                            countDownLatch.countDown();
                        }
                    }
                });
            } catch (RejectedExecutionException ree) {

                // 处理线程池满后的异常, 等待2s后重新提交
                logger.error("The thread pool has exceeded the maximum capacity, sleep 3s and submit again : " + ree.getMessage(), ree);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    logger.error("Interrupt during thread sleep");
                }
                this.listeningExecutorService.submit((Runnable) this);
            }
        }

        /**
         *   这里的超时时间需要设置比rpc超时时间短，否则没有意义
         */
        try {
            countDownLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("Interrupt during countDownLatch thread await", e);
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
