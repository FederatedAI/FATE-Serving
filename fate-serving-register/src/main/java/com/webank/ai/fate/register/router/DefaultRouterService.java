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
import com.webank.ai.fate.register.loadbalance.LoadBalancer;
import com.webank.ai.fate.register.url.CollectionUtils;
import com.webank.ai.fate.register.url.URL;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class DefaultRouterService extends AbstractRouterService {
    @Override
    public List<URL> doRouter(URL url, LoadBalancer loadBalancer) {
        List<URL> urls = registry.getCacheUrls(url);
        if (CollectionUtils.isEmpty(urls)) {
            return null;
        }
        urls = filterEmpty(urls);
        String version = url.getParameter(Constants.VERSION_KEY);
        if (CollectionUtils.isNotEmpty(urls) && StringUtils.isNotBlank(version)) {
            urls = filterVersion(urls, Long.parseLong(version));
        }
        List<URL> resultUrls = loadBalancer.select(urls);
        if (logger.isDebugEnabled()) {
            logger.debug("router service return urls {}", resultUrls);
        }
        return resultUrls;
    }

    private List<URL> filterEmpty(List<URL> urls) {
        List<URL> resultList = new ArrayList<>();
        if (urls != null) {
            urls.forEach(url -> {
                if (!url.getProtocol().equalsIgnoreCase(Constants.EMPTY_PROTOCOL)) {
                    resultList.add(url);
                }
            });
        }
        return resultList;
    }
}



