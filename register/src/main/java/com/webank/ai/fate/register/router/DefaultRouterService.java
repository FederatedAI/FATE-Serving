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

package com.webank.ai.fate.register.router;

import com.webank.ai.fate.register.common.Constants;
import com.webank.ai.fate.register.loadbalance.LoadBalanceModel;
import com.webank.ai.fate.register.url.CollectionUtils;
import com.webank.ai.fate.register.url.URL;
import org.apache.commons.lang.StringUtils;

import java.util.List;


public class DefaultRouterService extends AbstractRouterService {
    @Override
    public List<URL> doRouter(URL url, LoadBalanceModel loadBalanceModel) {

        List<URL> urls = registry.getCacheUrls(url);

        String version = url.getParameter(Constants.VERSION_KEY);
        if (CollectionUtils.isNotEmpty(urls) && StringUtils.isNotBlank(version)) {
            urls = filterVersion(urls, version);
        }
//        else{
//            AtomicReference<List<URL>>  resultUrls = new AtomicReference<>();
//            registry.subscribe(url  , resultUrls::set);
//            urls =resultUrls.get();
//            urls= filterVersion(urls,version);
//        }

        if (CollectionUtils.isEmpty(urls)) {
            return null;
        }

        List<URL> resultUrls = this.loadBalancer.select(urls);

        logger.info("router service return urls {}", resultUrls);

        return resultUrls;


    }


}



