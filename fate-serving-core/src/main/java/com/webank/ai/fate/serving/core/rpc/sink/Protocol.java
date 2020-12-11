package com.webank.ai.fate.serving.core.rpc.sink;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Protocol {
    String name();
}

