package com.webank.ai.fate.serving.proxy.common;


import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;


@RestControllerAdvice
/**
 *   全局的http结果返回处理器
 */
public class GlobalResponseController implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter methodParameter, Class converterType) {

        Boolean isRest = AnnotationUtils.isAnnotationDeclaredLocally(
                RestController.class, methodParameter.getContainingClass());
        ResponseBody responseBody = AnnotationUtils.findAnnotation(
                methodParameter.getMethod(), ResponseBody.class);

        if (responseBody != null || isRest) {
            return true;
        } else {
            return false;
        }

    }

    @Nullable
    @Override
    public Object beforeBodyWrite(@Nullable Object body,
                                  MethodParameter methodParameter,
                                  MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> aClass,
                                  ServerHttpRequest serverHttpRequest,
                                  ServerHttpResponse serverHttpResponse) {


        return body;

    }
}