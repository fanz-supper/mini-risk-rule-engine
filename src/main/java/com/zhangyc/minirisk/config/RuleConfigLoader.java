package com.zhangyc.minirisk.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangyc.minirisk.model.RiskContext;
import com.zhangyc.minirisk.model.Rule;
import com.zhangyc.minirisk.model.RuleAction;

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
     * 这里先用硬编码映射，后面可以用反射重构。
     */
    private static Predicate<RiskContext> buildSinglePredicate(ConditionDefinition c) {
        String field = c.getField();
        String op = c.getOp();
        String value = c.getValue();

        switch (field) {
            case "device.loginUserCountIn10Min":
                int thresholdLogin = Integer.parseInt(value);
                return ctx -> compareInt(ctx.getDeviceLoginUserCountIn10Min(), op, thresholdLogin);
            case "user.isNew":
                boolean expectNew = Boolean.parseBoolean(value);
                return ctx -> compareBoolean(ctx.isNewUser(), op, expectNew);
            case "user.historyOrderCount":
                int history = Integer.parseInt(value);
                return ctx -> compareInt(ctx.getHistoryOrderCount(), op, history);
            case "order.amount":
                double amount = Double.parseDouble(value);
                return ctx -> compareDouble(ctx.getOrderAmount(), op, amount);
            case "user.registerMinutes":
                int minutes = Integer.parseInt(value);
                return ctx -> compareInt(ctx.getRegisterMinutes(), op, minutes);
            case "ip.inBlacklist":
                boolean inBlack = Boolean.parseBoolean(value);
                return ctx -> compareBoolean(ctx.isIpInBlacklist(), op, inBlack);
            default:
                // 未知字段：永远不命中，或者你也可以选择抛异常
                return ctx -> false;
        }
    }

    /* 下面这三个比较方法，ExplainableRuleEngine 里也会用到，后续可以考虑提取为公共工具类 */

    public static boolean compareInt(int actual, String op, int expected) {
        switch (op) {
            case ">":
                return actual > expected;
            case ">=":
                return actual >= expected;
            case "<":
                return actual < expected;
            case "<=":
                return actual <= expected;
            case "==":
                return actual == expected;
            case "!=":
                return actual != expected;
            default:
                return false;
        }
    }

    public static boolean compareDouble(double actual, String op, double expected) {
        switch (op) {
            case ">":
                return actual > expected;
            case ">=":
                return actual >= expected;
            case "<":
                return actual < expected;
            case "<=":
                return actual <= expected;
            case "==":
                return Double.compare(actual, expected) == 0;
            case "!=":
                return Double.compare(actual, expected) != 0;
            default:
                return false;
        }
    }

    public static boolean compareBoolean(boolean actual, String op, boolean expected) {
        switch (op) {
            case "==":
                return actual == expected;
            case "!=":
                return actual != expected;
            default:
                return false;
        }
    }
}