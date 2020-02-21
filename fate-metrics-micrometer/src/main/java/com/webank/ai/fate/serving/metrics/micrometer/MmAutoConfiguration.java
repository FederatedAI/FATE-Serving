package com.webank.ai.fate.serving.metrics.micrometer;

import com.webank.ai.fate.serving.metrics.api.IMetricFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(IMetricFactory.class)
public class MmAutoConfiguration {
    @Bean("mmMetricFactory")
    @ConditionalOnMissingBean
    public IMetricFactory mmMetricFactory() {
        return new MmMetricFactory();
    }

    @Bean
    public MmMetricsRegistry mmMetricsRegistry() {
        return new MmMetricsRegistry();
    }
}
