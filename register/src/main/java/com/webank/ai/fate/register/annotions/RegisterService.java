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

package com.webank.ai.fate.register.annotions;

import com.webank.ai.fate.register.common.RouterModel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterService {

    String serviceName();

    String version() default "";

    boolean useDynamicEnvironment() default false;

    RouterModel routerModel() default RouterModel.ALL_ALLOWED;


//    String threadPoolKey() default "";
//
//    String fallbackMethod() default "";
//
//    HystrixProperty[] commandProperties() default {};
//
//    HystrixProperty[] threadPoolProperties() default {};
//
//    Class<? extends Throwable>[] ignoreExceptions() default {};
//
//    ObservableExecutionMode observableExecutionMode() default ObservableExecutionMode.EAGER;
//
//    HystrixException[] raiseHystrixExceptions() default {};
//
//    String defaultFallback() default "";


}
