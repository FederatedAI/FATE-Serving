package com.webank.ai.fate.serving.core.rpc.core;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface FateServiceMethod {
    String name();
}
