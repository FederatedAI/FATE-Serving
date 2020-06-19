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

package com.webank.ai.fate.register.task;

import com.webank.ai.fate.register.common.FailbackRegistry;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.serving.core.timer.Timeout;

public class FailedSubProjectTask extends AbstractRetryTask {

    private static final String NAME = "retry subscribe project";

    public FailedSubProjectTask(URL url, FailbackRegistry registry) {
        super(url, registry, NAME);
    }

    @Override
    protected void doRetry(URL url, FailbackRegistry registry, Timeout timeout) {
        registry.subProject(url.getProject());
        registry.removeFailedSubscribedProjectTask(url.getProject());
    }
}