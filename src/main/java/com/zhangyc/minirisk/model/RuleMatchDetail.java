package com.zhangyc.minirisk.model;

import java.util.Collections;
import java.util.List;

/**
 * 描述「某条规则」在一次风控评估中的命中细节：
 * - 是否整体命中
 * - 每个条件的评估结果
 */
public class RuleMatchDetail {

    /** 对应的规则 */
    private final Rule rule;

    /** 这条规则最终是否命中（条件组合后结果） */
    private final boolean matched;

    /** 该规则下每个条件的评估信息 */
    private final List<ConditionMatch> conditionMatches;

    public RuleMatchDetail(Rule rule, boolean matched, List<ConditionMatch> conditionMatches) {
        this.rule = rule;
        this.matched = matched;
        this.conditionMatches = conditionMatches == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(conditionMatches);
    }

    public Rule getRule() {
        return rule;
    }

    public boolean isMatched() {
        return matched;
    }

    public List<ConditionMatch> getConditionMatches() {
        return conditionMatches;
    }

    @Override
    public String toString() {
        return "RuleMatchDetail{" +
                "rule=" + rule +
                ", matched=" + matched +
                ", conditionMatches=" + conditionMatches +
                '}';
    }
}

