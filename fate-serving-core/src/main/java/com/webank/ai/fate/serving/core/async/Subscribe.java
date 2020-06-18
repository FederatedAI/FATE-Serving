package com.webank.ai.fate.serving.core.async;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Subscribe {

    String value() default "";

    String name() default "";
}
