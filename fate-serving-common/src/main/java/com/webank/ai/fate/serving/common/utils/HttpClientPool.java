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
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.utils.ObjectTransform;
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

public class HttpClientPool {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientPool.class);
    private static PoolingHttpClientConnectionManager poolConnManager;
    private static RequestConfig requestConfig;
    private static CloseableHttpClient httpClient;

    private static void config(HttpRequestBase httpRequestBase, Map<String, String> headers) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(MetaInfo.HTTP_CLIENT_CONFIG_CONN_REQ_TIME_OUT)
                .setConnectTimeout(MetaInfo.HTTP_CLIENT_CONFIG_CONN_TIME_OUT)
                .setSocketTimeout(MetaInfo.HTTP_CLIENT_CONFIG_SOCK_TIME_OUT).build();
        httpRequestBase.addHeader(Dict.CONTENT_TYPE, Dict.CONTENT_TYPE_JSON_UTF8);
        if (headers != null) {
            headers.forEach(httpRequestBase::addHeader);
        }
        httpRequestBase.setConfig(requestConfig);
    }

    public static void initPool() {
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register(
                    Dict.HTTP, PlainConnectionSocketFactory.getSocketFactory()).register(
                    Dict.HTTPS, sslsf).build();
            poolConnManager = new PoolingHttpClientConnectionManager(
                    socketFactoryRegistry);
            poolConnManager.setMaxTotal(MetaInfo.HTTP_CLIENT_INIT_POOL_MAX_TOTAL);
            poolConnManager.setDefaultMaxPerRoute(MetaInfo.HTTP_CLIENT_INIT_POOL_DEF_MAX_PER_ROUTE);
            int socketTimeout = MetaInfo.HTTP_CLIENT_INIT_POOL_SOCK_TIME_OUT;
            int connectTimeout = MetaInfo.HTTP_CLIENT_INIT_POOL_CONN_TIME_OUT;
            int connectionRequestTimeout = MetaInfo.HTTP_CLIENT_INIT_POOL_CONN_REQ_TIME_OUT;
            requestConfig = RequestConfig.custom().setConnectionRequestTimeout(
                    connectionRequestTimeout).setSocketTimeout(socketTimeout).setConnectTimeout(
                    connectTimeout).build();
            httpClient = createConnection();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException ex) {
            logger.error("init http client pool failed:", ex);
        }
    }
    public static CloseableHttpClient getConnection() {
        return httpClient;
    }

    public static CloseableHttpClient createConnection() {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(poolConnManager)
                .setDefaultRequestConfig(requestConfig)
                .evictExpiredConnections()
                .evictIdleConnections(5, TimeUnit.SECONDS)
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                .build();
        return httpClient;
    }

    public static String post(String url, Map<String, Object> requestData) {
        return sendPost(url, requestData, null);
    }

    public static String post(String url, Map<String, Object> requestData, Map<String, String> headers) {
        return sendPost(url, requestData, headers);
    }

    public static String sendPost(String url, Map<String, Object> requestData, Map<String, String> headers) {
        HttpPost httpPost = new HttpPost(url);
        config(httpPost, headers);
        StringEntity stringEntity = new StringEntity(ObjectTransform.bean2Json(requestData), Dict.CHARSET_UTF8);
        stringEntity.setContentEncoding(Dict.CHARSET_UTF8);
        httpPost.setEntity(stringEntity);
        return getResponse(httpPost);
    }
    public static String sendPost(String url, String requestData, Map<String, String> headers) {
        HttpPost httpPost = new HttpPost(url);
        config(httpPost, headers);
        StringEntity stringEntity = new StringEntity(requestData, Dict.CHARSET_UTF8);
        stringEntity.setContentEncoding(Dict.CHARSET_UTF8);
        httpPost.setEntity(stringEntity);
        return getResponse(httpPost);
    }

    public static String get(String url, Map<String, String> headers) {
        return sendGet(url, headers);
    }

    public static String get(String url) {
        return sendGet(url, null);
    }

    public static String sendGet(String url, Map<String, String> headers) {
        HttpGet httpGet = new HttpGet(url);
        config(httpGet, headers);
        return getResponse(httpGet);
    }

    private static String getResponse(HttpRequestBase request) {
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request,
                    HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, Dict.CHARSET_UTF8);
            EntityUtils.consume(entity);
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

    public static String transferPost(String url, Map<String, Object> requestData) {
        HttpPost httpPost = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(MetaInfo.HTTP_CLIENT_TRAN_CONN_REQ_TIME_OUT)
                .setConnectTimeout(MetaInfo.HTTP_CLIENT_TRAN_CONN_TIME_OUT)
                .setSocketTimeout(MetaInfo.HTTP_CLIENT_TRAN_SOCK_TIME_OUT).build();
        httpPost.addHeader(Dict.CONTENT_TYPE, Dict.CONTENT_TYPE_JSON_UTF8);
        httpPost.setConfig(requestConfig);
        StringEntity stringEntity = new StringEntity(ObjectTransform.bean2Json(requestData), Dict.CHARSET_UTF8);
        stringEntity.setContentEncoding(Dict.CHARSET_UTF8);
        httpPost.setEntity(stringEntity);
        return getResponse(httpPost);
    }
}
