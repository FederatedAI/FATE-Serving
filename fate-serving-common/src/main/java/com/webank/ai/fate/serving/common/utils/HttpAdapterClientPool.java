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

package com.webank.ai.fate.serving.common.utils;

import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.HttpAdapterResponse;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.core.utils.ObjectTransform;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpAdapterClientPool {
    private static final Logger logger = LoggerFactory.getLogger(HttpAdapterClientPool.class);
    private static CloseableHttpClient httpClient;

    public static HttpAdapterResponse doPost(String url, Map<String, Object> bodyMap) {
        HttpPost httpPost = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(MetaInfo.PROPERTY_HTTP_CONNECT_REQUEST_TIMEOUT)
                .setConnectTimeout(MetaInfo.PROPERTY_HTTP_CONNECT_TIMEOUT)
                .setSocketTimeout(MetaInfo.PROPERTY_HTTP_SOCKET_TIMEOUT).build();
        httpPost.addHeader(Dict.CONTENT_TYPE, Dict.CONTENT_TYPE_JSON_UTF8);
        httpPost.setConfig(requestConfig);
        String bodyJson = JsonUtil.object2Json(bodyMap);
        StringEntity stringEntity = new StringEntity(bodyJson, Dict.CHARSET_UTF8);
        stringEntity.setContentEncoding(Dict.CHARSET_UTF8);
        httpPost.setEntity(stringEntity);
        logger.info(" postUrl = {"+url+"}  body = {"+bodyJson+"} ");
        return getResponse(httpPost);
    }

    private static HttpAdapterResponse getResponse(HttpRequestBase request) {
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request,
                    HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, Dict.CHARSET_UTF8);
            return JsonUtil.json2Object(result, HttpAdapterResponse.class);
        } catch (IOException ex) {
            logger.error("get http response failed:", ex);
            return null;
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException ex) {
                logger.error("get http response failed:", ex);
            }
        }
    }


    public static HttpAdapterResponse doPostgetCodeByHeader(String url, Map<String, Object> bodyMap) {
        HttpPost httpPost = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(MetaInfo.PROPERTY_HTTP_CONNECT_REQUEST_TIMEOUT)
                .setConnectTimeout(MetaInfo.PROPERTY_HTTP_CONNECT_TIMEOUT)
                .setSocketTimeout(MetaInfo.PROPERTY_HTTP_SOCKET_TIMEOUT).build();
        httpPost.addHeader(Dict.CONTENT_TYPE, Dict.CONTENT_TYPE_JSON_UTF8);
        httpPost.setConfig(requestConfig);
        String bodyJson = JsonUtil.object2Json(bodyMap);
        StringEntity stringEntity = new StringEntity(bodyJson, Dict.CHARSET_UTF8);
        stringEntity.setContentEncoding(Dict.CHARSET_UTF8);
        httpPost.setEntity(stringEntity);
        logger.info(" postUrl = {"+url+"}  body = {"+bodyJson+"} ");
        return getResponseByHeader(httpPost);
    }

    private static HttpAdapterResponse getResponseByHeader(HttpRequestBase request) {
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request,
                    HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            String data = EntityUtils.toString(entity, Dict.CHARSET_UTF8);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpAdapterResponse result = new HttpAdapterResponse();
            result.setCode(statusCode);
            result.setData(JsonUtil.json2Object(data,Map.class));
            return result;
        } catch (IOException ex) {
            logger.error("get http response failed:", ex);
            return null;
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException ex) {
                logger.error("get http response failed:", ex);
            }
        }
    }
}
