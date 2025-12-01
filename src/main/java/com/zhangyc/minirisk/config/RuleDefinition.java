package com.zhangyc.minirisk.config;

import java.util.List;

/**
 * 从 JSON 中读到的“规则配置模型”。
 * 注意：这里的 condition 还只是字符串形式，后面会转换为真正的 Predicate。
 */
public class RuleDefinition {

    private String id;
    private String description;
    private String scene;
    private int priority;
    private String action;       // "ALLOW" / "REJECT" / "MANUAL_REVIEW"
    private String logicalOp;    // "AND" / "OR"（目前只用 AND）
    private List<ConditionDefinition> conditions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getLogicalOp() {
        return logicalOp;
    }

    public void setLogicalOp(String logicalOp) {
        this.logicalOp = logicalOp;
    }

    public List<ConditionDefinition> getConditions() {
        return conditions;
    }

    public void setConditions(List<ConditionDefinition> conditions) {
        this.conditions = conditions;
    }
}
