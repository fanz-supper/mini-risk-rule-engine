package com.zhangyc.minirisk.registry;

import com.zhangyc.minirisk.config.RuleConfigLoader;
import com.zhangyc.minirisk.model.Rule;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 规则注册中心 / 规则仓库：
 * - 启动时从配置文件加载所有规则；
 * - 提供按场景获取规则的方法。
 */
public class RuleRegistry {

    // 全部规则缓存（不可变列表）
    private static final List<Rule> ALL_RULES;

    static {
        // 这里写死从哪个配置文件加载；后面也可以改成可配置的
        ALL_RULES = Collections.unmodifiableList(
                RuleConfigLoader.loadRulesFromClasspath("rules-demo.json")
        );
    }

    private RuleRegistry() {
        // 工具类，不允许实例化
    }

    /**
     * 获取指定场景下要执行的规则：
     * - 包括 scene 完全匹配的规则；
     * - 以及 scene = "COMMON" 的通用规则。
     *
     * @param scene 例如 "PAY" / "LOGIN" / "REGISTER"
     */
    public static List<Rule> getRulesForScene(String scene) {
        Objects.requireNonNull(scene, "scene must not be null");

        return ALL_RULES.stream()
                .filter(rule ->
                        scene.equalsIgnoreCase(rule.getScene())
                                || "COMMON".equalsIgnoreCase(rule.getScene())
                )
                .collect(Collectors.toList());
    }

    /**
     * 如果你真的想拿到全部规则，也可以提供这个方法。
     */
    public static List<Rule> getAllRules() {
        return ALL_RULES;
    }
}
