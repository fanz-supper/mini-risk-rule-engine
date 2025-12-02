package com.zhangyc.minirisk.support;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * 标记 RiskContext 中的 getter 方法，对应规则中的字段路径：
 * 例如字段路径 "user.isNew" -> 注解写在 isNewUser() 上。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RiskField {

    /**
     * 规则里使用的字段路径，例如 "user.isNew"、"order.amount" 等。
     */
    String value();
}

