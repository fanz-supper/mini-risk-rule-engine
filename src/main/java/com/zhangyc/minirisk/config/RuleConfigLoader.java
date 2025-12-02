package com.zhangyc.minirisk.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangyc.minirisk.model.RiskContext;
import com.zhangyc.minirisk.model.Rule;
import com.zhangyc.minirisk.model.RuleAction;
import com.zhangyc.minirisk.support.RiskFieldAccessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;

/**
 * 从 JSON 配置文件加载规则，并转换为真正可执行的 Rule 列表，
 * 同时缓存 RuleDefinition 以支持后续解释。
 */
public class RuleConfigLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** ruleId -> RuleDefinition 的映射，用于解释层 */
    private static final Map<String, RuleDefinition> RULE_DEFINITION_MAP = new HashMap<>();

    /**
     * 从 classpath（resources） 下加载 JSON 配置，并转换为 Rule 列表。
     *
     * @param resourceName 例如 "rules-demo.json"
     */
    public static List<Rule> loadRulesFromClasspath(String resourceName) {
        try (InputStream in = RuleConfigLoader.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IllegalArgumentException("规则配置文件未找到: " + resourceName);
            }

            // 1. 先解析为 RuleDefinition 列表
            // MAPPER.readValue会自动识别传入的class里的字段，并把相应的字段赋值给对应的成员变量
            // TypeReference<List<RuleDefinition>>() 是解决泛型类型擦除，告诉list中的T是RuleDefinition
            List<RuleDefinition> defs = MAPPER.readValue(
                    in,
                    new TypeReference<List<RuleDefinition>>() {}
            );

            // 2. 缓存 RuleDefinition，方便后续解释使用
            RULE_DEFINITION_MAP.clear();
            for (RuleDefinition def : defs) {
                RULE_DEFINITION_MAP.put(def.getId(), def);
            }

            // 3. 再把每个 RuleDefinition 转为真正的 Rule（带 Predicate<RiskContext>）
            List<Rule> rules = new ArrayList<>();
            for (RuleDefinition def : defs) {
                Rule rule = convertToRule(def);
                rules.add(rule);
            }
            return rules;
        } catch (IOException e) {
            throw new RuntimeException("加载规则配置失败: " + resourceName, e);
        }
    }

    /**
     * 提供按 ruleId 获取 RuleDefinition 的方法，给解释引擎用。
     */
    public static RuleDefinition getRuleDefinitionById(String ruleId) {
        return RULE_DEFINITION_MAP.get(ruleId);
    }

    private static Rule convertToRule(RuleDefinition def) {
        RuleAction action = RuleAction.valueOf(def.getAction().toUpperCase(Locale.ROOT));
        Predicate<RiskContext> condition = buildConditionPredicate(def);
        return new Rule(
                def.getId(),
                def.getDescription(),
                def.getScene(),
                def.getPriority(),
                condition,
                action
        );
    }

    /**
     * 根据 RuleDefinition 中的 conditions + logicalOp 构造一个 Predicate<RiskContext>。
     */
    private static Predicate<RiskContext> buildConditionPredicate(RuleDefinition def) {
        List<ConditionDefinition> conds = def.getConditions();
        if (conds == null || conds.isEmpty()) {
            // 没配置条件，则永远不命中（也可以设计成永远命中，看你需求）
            return ctx -> false;
        }

        List<Predicate<RiskContext>> predicateList = new ArrayList<>();
        for (ConditionDefinition c : conds) {
            predicateList.add(buildSinglePredicate(c));
        }

        // 目前只支持 AND / OR
        String op = def.getLogicalOp();
        if (op == null || op.isEmpty() || "AND".equalsIgnoreCase(op)) {
            // AND：所有条件都满足才为 true
            Predicate<RiskContext> result = predicateList.get(0);
            for (int i = 1; i < predicateList.size(); i++) {
                result = result.and(predicateList.get(i));
            }
            return result;
        } else if ("OR".equalsIgnoreCase(op)) {
            // OR：任意一个满足为 true
            Predicate<RiskContext> result = predicateList.get(0);
            for (int i = 1; i < predicateList.size(); i++) {
                result = result.or(predicateList.get(i));
            }
            return result;
        } else {
            // 未知逻辑操作符，简单处理：当作 AND
            Predicate<RiskContext> result = predicateList.get(0);
            for (int i = 1; i < predicateList.size(); i++) {
                result = result.and(predicateList.get(i));
            }
            return result;
        }
    }

    /**
     * 单个条件：根据 field / op / value 构造一个基于 RiskContext 的谓词。
     * 统一走 evaluateCondition + RiskFieldAccessor，不再写死字段映射。
     */
    private static Predicate<RiskContext> buildSinglePredicate(ConditionDefinition c) {
        return ctx -> evaluateCondition(ctx, c);
    }

    /**
     * 根据 ConditionDefinition 和上下文，执行单个条件判断。
     * 使用 RiskFieldAccessor 动态读取字段值，并做类型感知的比较。
     */
    public static boolean evaluateCondition(RiskContext ctx, ConditionDefinition c) {
        Object actual = RiskFieldAccessor.getFieldValue(ctx, c.getField());
        String op = c.getOp();
        String expectedStr = c.getValue();

        return compareValue(actual, op, expectedStr);
    }

    /**
     * 通用比较逻辑：
     * - 如果 actual 是 Number，按 double 比较
     * - 如果 actual 是 Boolean，按 boolean 比较
     * - 其他类型当作字符串比较（只支持 == / !=）
     */
    public static boolean compareValue(Object actual, String op, String expectedStr) {
        if (actual == null) {
            return false;
        }

        if (actual instanceof Number) {
            double actualD = ((Number) actual).doubleValue();
            double expectedD = Double.parseDouble(expectedStr);
            switch (op) {
                case ">":
                    return actualD > expectedD;
                case ">=":
                    return actualD >= expectedD;
                case "<":
                    return actualD < expectedD;
                case "<=":
                    return actualD <= expectedD;
                case "==":
                    return Double.compare(actualD, expectedD) == 0;
                case "!=":
                    return Double.compare(actualD, expectedD) != 0;
                default:
                    return false;
            }
        } else if (actual instanceof Boolean) {
            boolean actualB = (Boolean) actual;
            boolean expectedB = Boolean.parseBoolean(expectedStr);
            switch (op) {
                case "==":
                    return actualB == expectedB;
                case "!=":
                    return actualB != expectedB;
                default:
                    return false;
            }
        } else {
            // 其他当成字符串
            String actualS = String.valueOf(actual);
            switch (op) {
                case "==":
                    return actualS.equals(expectedStr);
                case "!=":
                    return !actualS.equals(expectedStr);
                default:
                    return false;
            }
        }
    }
}