package com.webank.ai.fate.serving.admin.config;

import com.webank.ai.fate.serving.core.bean.Dict;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("X-Requested-With", "accept", "content-type", Dict.SESSION_TOKEN)
                .allowCredentials(true)
                .maxAge(86400);
    }
}
