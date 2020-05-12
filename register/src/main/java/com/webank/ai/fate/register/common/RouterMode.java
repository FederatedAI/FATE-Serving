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

package com.webank.ai.fate.register.common;

public enum RouterMode {
    /**
     * VERSION_BIGER
     */
    VERSION_BIGER,
    /**
     * VERSION_BIGTHAN_OR_EQUAL
     */
    VERSION_BIGTHAN_OR_EQUAL,
    /**
     * VERSION_SMALLER
     */
    VERSION_SMALLER,
    /**
     * VERSION_EQUALS
     */
    VERSION_EQUALS,
    /**
     * ALL_ALLOWED
     */
    ALL_ALLOWED;

    public static boolean contains(String routerMode) {
        RouterMode[] values = RouterMode.values();
        for (RouterMode value : values) {
            if (String.valueOf(value).equals(routerMode)) {
                return true;
            }
        }
        return false;
    }

}
