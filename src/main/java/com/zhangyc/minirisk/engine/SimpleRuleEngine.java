package com.zhangyc.minirisk.engine;

import com.zhangyc.minirisk.model.DecisionResult;
import com.zhangyc.minirisk.model.RiskContext;
import com.zhangyc.minirisk.model.Rule;
import com.zhangyc.minirisk.model.RuleAction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 最简单的规则引擎实现：
 * - 逐条遍历规则，筛选命中的；
 * - 按优先级排序；
 * - 根据规则动作合成最终决策。
 */
public class SimpleRuleEngine implements RuleEngine {

    @Override
    public DecisionResult evaluate(RiskContext context, List<Rule> rules) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(rules, "rules must not be null");

        List<Rule> matched = new ArrayList<>();

        for (Rule rule : rules) {
            if (rule.getCondition().test(context)) {
                matched.add(rule);
            }
        }

        if (matched.isEmpty()) {
            // 没有命中任何规则，默认放行
            return new DecisionResult(RuleAction.ALLOW, matched);
        }

        // 按优先级从高到低排序
        matched.sort(Comparator.comparingInt(Rule::getPriority).reversed());

        // 计算最终动作
        RuleAction finalAction = calculateFinalAction(matched);
        return new DecisionResult(finalAction, matched);
    }

    private RuleAction calculateFinalAction(List<Rule> matched) {
        boolean hasReject = matched.stream()
                .anyMatch(rule -> rule.getAction() == RuleAction.REJECT);
        if (hasReject) {
            return RuleAction.REJECT;
        }

        boolean hasManual = matched.stream()
                .anyMatch(rule -> rule.getAction() == RuleAction.MANUAL_REVIEW);
        if (hasManual) {
            return RuleAction.MANUAL_REVIEW;
        }

        return RuleAction.ALLOW;
    }
}
