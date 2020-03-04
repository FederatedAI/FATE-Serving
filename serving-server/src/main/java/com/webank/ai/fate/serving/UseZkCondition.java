package com.webank.ai.fate.serving;

import com.webank.ai.fate.serving.core.bean.Dict;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class UseZkCondition implements Condition {
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {

        Environment environment = conditionContext.getEnvironment();

        boolean useZk = environment.getProperty(Dict.USE_REGISTER, Boolean.class, Boolean.TRUE);

        return useZk;
    }
}
