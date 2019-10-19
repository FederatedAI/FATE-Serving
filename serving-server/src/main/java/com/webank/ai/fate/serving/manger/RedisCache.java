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

package com.webank.ai.fate.serving.manger;

import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.interfaces.Cache;

/**
 * @Description TODO
 * @Author kaideng
 **/
public class RedisCache implements Cache {
    @Override
    public void put(Context context, String key, Object object) {

    }

    @Override
    public <T> T get(Context context, String key, Class<T> dataType) {
        return null;
    }
}