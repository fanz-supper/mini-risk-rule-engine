package com.zhangyc.minirisk.model;

import java.util.Collections;
import java.util.List;

/**
 * 一次风控决策的最终结果 + 命中规则列表。
 */
public class DecisionResult {

    private final RuleAction finalAction;
    private final List<Rule> matchedRules;

    public DecisionResult(RuleAction finalAction, List<Rule> matchedRules) {
        this.finalAction = finalAction;
        this.matchedRules = matchedRules == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(matchedRules);
    }

    public RuleAction getFinalAction() {
        return finalAction;
    }

    public List<Rule> getMatchedRules() {
        return matchedRules;
    }

    @Override
    public String toString() {
        return "DecisionResult{" +
                "finalAction=" + finalAction +
                ", matchedRules=" + matchedRules +
                '}';
    }
}
