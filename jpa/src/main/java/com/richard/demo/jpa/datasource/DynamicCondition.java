package com.richard.demo.jpa.datasource;

import com.richard.demo.basic.util.SpringContext;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class DynamicCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return isDynamic(context);
    }

    private boolean isDynamic(ConditionContext context) {
        return SpringContext.isTrue("richard.datasource.jpa-dynamic-enabled", context.getEnvironment());
    }

    class Contrary implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return !isDynamic(context);
        }
    }
}
