package com.zhangyc.minirisk.support;

import com.zhangyc.minirisk.model.RiskContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用反射，根据字段路径（例如 "user.isNew"）从 RiskContext 中动态获取实际值。
 * 这是一个小型字段访问层，避免在各处写大量的 switch(field)。
 */
public final class RiskFieldAccessor {

    /**
     * 字段路径 -> 对应的 getter 方法
     * 例如 "user.isNew" -> RiskContext.isNewUser()
     */
    private static final Map<String, Method> FIELD_METHOD_MAP = new HashMap<>();

    static {
        try {
            // 这里做“字段路径”到 RiskContext getter 的映射
            // 只在类加载时反射一次，后续都是直接调用 Method，开销很小。

            FIELD_METHOD_MAP.put("user.isNew",
                    RiskContext.class.getMethod("isNewUser"));

            FIELD_METHOD_MAP.put("user.historyOrderCount",
                    RiskContext.class.getMethod("getHistoryOrderCount"));

            FIELD_METHOD_MAP.put("user.registerMinutes",
                    RiskContext.class.getMethod("getRegisterMinutes"));

            FIELD_METHOD_MAP.put("order.amount",
                    RiskContext.class.getMethod("getOrderAmount"));

            FIELD_METHOD_MAP.put("device.loginUserCountIn10Min",
                    RiskContext.class.getMethod("getDeviceLoginUserCountIn10Min"));

            FIELD_METHOD_MAP.put("ip.inBlacklist",
                    RiskContext.class.getMethod("isIpInBlacklist"));

        } catch (NoSuchMethodException e) {
            // 如果这里出错，基本就是我们自己维护映射写错了方法名，
            // 直接抛出 RuntimeException，方便在开发期暴露问题。
            throw new IllegalStateException("初始化 RiskFieldAccessor 失败，请检查映射的 getter 方法名是否正确", e);
        }
    }

    private RiskFieldAccessor() {
        // 工具类，不允许实例化
    }

    /**
     * 根据字段路径，从给定的 context 中取出对应的实际值。
     *
     * @param context   风控上下文
     * @param fieldPath 字段路径，例如 "user.isNew"
     * @return getter 返回的实际值（可能是 Boolean / Integer / Double 等）
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
