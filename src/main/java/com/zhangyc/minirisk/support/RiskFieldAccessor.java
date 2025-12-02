package com.zhangyc.minirisk.support;

import com.zhangyc.minirisk.model.RiskContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用反射 + 注解，根据字段路径（例如 "user.isNew"）从 RiskContext 中动态获取值。
 */
public final class RiskFieldAccessor {

    /**
     * 字段路径 -> 对应 getter 方法
     */
    private static final Map<String, Method> FIELD_METHOD_MAP = new HashMap<>();

    static {
        initFieldMethodMap();
    }

    private RiskFieldAccessor() {
    }

    private static void initFieldMethodMap() {
        Method[] methods = RiskContext.class.getMethods();
        for (Method method : methods) {
            RiskField annotation = method.getAnnotation(RiskField.class);
            if (annotation == null) {
                continue;
            }
            String fieldPath = annotation.value();
            if (FIELD_METHOD_MAP.containsKey(fieldPath)) {
                throw new IllegalStateException(
                        "字段路径重复映射: " + fieldPath +
                                ", 已存在方法=" + FIELD_METHOD_MAP.get(fieldPath).getName() +
                                ", 新方法=" + method.getName()
                );
            }
            FIELD_METHOD_MAP.put(fieldPath, method);
        }

        if (FIELD_METHOD_MAP.isEmpty()) {
            System.err.println("[RiskFieldAccessor] 警告：未扫描到任何 @RiskField 注解映射。");
        }
    }

    /**
     * 根据字段路径，从给定 context 中取值。
     */
    public static Object getFieldValue(RiskContext context, String fieldPath) {
        Method method = FIELD_METHOD_MAP.get(fieldPath);
        if (method == null) {
            throw new IllegalArgumentException("未知字段路径: " + fieldPath);
        }
        try {
            return method.invoke(context);
        } catch (Exception e) {
            throw new RuntimeException("通过反射读取字段值失败: " + fieldPath, e);
        }
    }
}

