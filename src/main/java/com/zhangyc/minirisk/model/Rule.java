package com.zhangyc.minirisk.model;

import java.util.function.Predicate;

/**
 * 单条风控规则。
 */
public class Rule {

    private final String id;
    private final String description;
    private final String scene; // 比如 LOGIN / PAY / REGISTER 等
    private final int priority; // 数字越大优先级越高
    private final Predicate<RiskContext> condition;
    private final RuleAction action;

    public Rule(String id,
                String description,
                String scene,
                int priority,
                Predicate<RiskContext> condition,
                RuleAction action) {
        this.id = id;
        this.description = description;
        this.scene = scene;
        this.priority = priority;
        this.condition = condition;
        this.action = action;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getScene() {
        return scene;
    }

    public int getPriority() {
        return priority;
    }

    public Predicate<RiskContext> getCondition() {
        return condition;
    }

    public RuleAction getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", scene='" + scene + '\'' +
                ", priority=" + priority +
                ", action=" + action +
                '}';
    }
}
