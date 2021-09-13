package com.webank.ai.fate.serving.common.health;

import com.webank.ai.fate.serving.core.bean.Context;

public interface HealthCheckAware {

    public   HealthCheckResult   check(Context context );
}
