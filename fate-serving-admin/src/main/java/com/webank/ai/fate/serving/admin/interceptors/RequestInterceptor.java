package com.webank.ai.fate.serving.admin.interceptors;

import com.webank.ai.fate.serving.admin.cache.MetricCache;
import com.webank.ai.fate.serving.core.bean.MetricEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestInterceptor.class);

    @Autowired
    private MetricCache metricCache;

    @Value("${monitor.user.auth.key}")
    private String userAuthKey;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (response.getStatus() == HttpStatus.OK.value()) {

            MetricEntity entity = new MetricEntity();
            entity.setUserAuthKey(userAuthKey);
            entity.setComponentName("serving-server");
            entity.setInterfaceName(request.getRequestURI());
            entity.setPassQps(1L);
            entity.setTimestamp(System.currentTimeMillis());

            // serving-server__/api/model/publishLoad__1582616571000
            String key = buildMetricKey(entity.getComponentName(), entity.getInterfaceName(), entity.getTimestamp());

            MetricEntity cacheEntity = metricCache.get(key);
            if (cacheEntity != null) {
                // 相同时间（秒）累加qps
                cacheEntity.addPassQps(entity.getPassQps());
            } else {
                // 清除毫秒
                entity.setTimestamp(entity.getTimestamp() / 1000 * 1000);
                metricCache.put(key, entity);
                metricCache.addResource(buildResourceKey(entity.getComponentName(), entity.getInterfaceName()));
            }
        }
    }

    private String buildMetricKey(String componentName, String interfaceName, Long timestamp) {
        return componentName + "__" + interfaceName + "__" + (timestamp / 1000 * 1000);
    }

    private String buildResourceKey(String componentName, String interfaceName) {
        return componentName + "__" + interfaceName + "__";
    }


}
