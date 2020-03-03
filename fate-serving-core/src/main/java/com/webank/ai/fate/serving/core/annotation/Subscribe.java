package com.webank.ai.fate.serving.core.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Subscribe {
    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";
}
