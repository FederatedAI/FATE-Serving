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

package com.webank.ai.fate.serving.interfaces;

import com.webank.ai.fate.serving.federatedml.PipelineTask;

import java.util.Set;

public interface ModelCache {

    public void put(String modelKey, PipelineTask model);

    public PipelineTask get(String modelKey);

    public long getSize();

    public Set<String> getKeys();

    public PipelineTask loadModel(String modelKey);

}
