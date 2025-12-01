package com.zhangyc.minirisk.model;

import java.util.Collections;
import java.util.List;

/**
 * 一次风控决策的最终结果 + 命中规则列表 +（可选）命中细节。
 */
public class DecisionResult {

    private final RuleAction finalAction;
    private final List<Rule> matchedRules;

    /** 新增：每条规则的命中解释信息（可选） */
    private final List<RuleMatchDetail> ruleMatchDetails;

    /**
     * 兼容之前的构造方法：只有 finalAction 和 matchedRules。
     * 这种情况下，ruleMatchDetails 默认为空列表。
     */
    public DecisionResult(RuleAction finalAction, List<Rule> matchedRules) {
        this(finalAction, matchedRules, null);
    }

    /**
     * 新构造方法：允许传入规则命中细节。
     */
    public DecisionResult(RuleAction finalAction,
                          List<Rule> matchedRules,
                          List<RuleMatchDetail> ruleMatchDetails) {
        this.finalAction = finalAction;
        this.matchedRules = matchedRules == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(matchedRules);
        this.ruleMatchDetails = ruleMatchDetails == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(ruleMatchDetails);
    }

    public RuleAction getFinalAction() {
        return finalAction;
    }

    public List<Rule> getMatchedRules() {
        return matchedRules;
    }

    public List<RuleMatchDetail> getRuleMatchDetails() {
        return ruleMatchDetails;
    }

    @Override
    public String toString() {
        return "DecisionResult{" +
                "finalAction=" + finalAction +
                ", matchedRules=" + matchedRules +
                ", ruleMatchDetails=" + ruleMatchDetails +
                '}';
    }
}
