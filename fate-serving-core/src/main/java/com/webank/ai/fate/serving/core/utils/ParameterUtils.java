package com.webank.ai.fate.serving.core.utils;

import com.webank.ai.fate.serving.core.exceptions.ParameterException;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @auther Xiongli
 * @date 2021/7/30
 * @remark
 */
public class ParameterUtils {
    public static void checkArgument(boolean expression, @Nullable String errorMessage) {
        if (!expression) {
            throw new ParameterException(errorMessage);
        }
    }
}
