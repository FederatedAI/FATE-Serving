package com.webank.ai.fate.serving.common.health;

import com.webank.ai.fate.serving.core.bean.Context;

public interface HealthCheckAware {

    HealthCheckResult check(Context context);
}
