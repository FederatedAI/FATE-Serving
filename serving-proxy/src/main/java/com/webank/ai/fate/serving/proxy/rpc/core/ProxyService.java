//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.webank.ai.fate.serving.proxy.rpc.core;


import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ProxyService {
    String name();
    String[] preChain() default {};
    String[]  postChain() default {};

}
