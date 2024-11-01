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

package com.webank.ai.fate.serving.proxy.utils;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ToStringUtils {
    private static final String NULL_RESULT = "[null]";

    private static final Logger logger = LoggerFactory.getLogger(ToStringUtils.class);

    private final JsonFormat.Printer protoPrinter = JsonFormat.printer()
            .preservingProtoFieldNames()
            .omittingInsignificantWhitespace();

    public String toOneLineString(Message target) {
        String result;

        if (target == null) {
            return NULL_RESULT;
        }

        try {
            result = protoPrinter.print(target);
        } catch (Exception e) {
            logger.info(ExceptionUtils.getStackTrace(e));
            return NULL_RESULT;
        }

        return result;
    }
}
