package com.zhangyc.minirisk.demo;

import com.zhangyc.minirisk.engine.ExplainableRuleEngine;
import com.zhangyc.minirisk.engine.RuleEngine;
import com.zhangyc.minirisk.engine.SimpleRuleEngine;
import com.zhangyc.minirisk.model.*;
import com.zhangyc.minirisk.registry.RuleRegistry;

import java.util.List;

/**
 * v0.3 Demo：采用带解释能力的规则引擎，按场景（PAY）执行风控决策。
 */
public class DemoApplication3 {

    public static void main(String[] args) {
        // 1. 当前场景
        String scene = "PAY";

        // 2. 从规则中心取规则
        List<Rule> rules = RuleRegistry.getRulesForScene(scene);

        // 3. 构造上下文
        RiskContext ctx = new RiskContext()
                .setUserId("U123")
                .setNewUser(true)
                .setRegisterMinutes(30)
                .setHistoryOrderCount(0)
                .setOrderId("O20250101")
                .setOrderAmount(1500.0)
                .setDeviceId("D001")
                .setDeviceLoginUserCountIn10Min(1)
                .setIp("1.2.3.4")
                .setIpInBlacklist(false);

        // 4. 使用带解释能力的引擎：内部委托 SimpleRuleEngine 做基础决策
        RuleEngine engine = new ExplainableRuleEngine(new SimpleRuleEngine());
        DecisionResult result = engine.evaluate(ctx, rules);

        // 5. 打印结果
        System.out.println("Scene: " + scene);
        System.out.println("Context: " + ctx);
        System.out.println("Final Decision: " + result.getFinalAction());
        System.out.println("Matched rules:");
        for (Rule rule : result.getMatchedRules()) {
            System.out.println("  - " + rule.getId() + " | " + rule.getDescription() +
                    " | action=" + rule.getAction() +
                    " | priority=" + rule.getPriority() +
                    " | scene=" + rule.getScene());
        }

        // 6. 打印详细解释
        System.out.println();
        System.out.println("=== Explanation Details ===");
        for (RuleMatchDetail detail : result.getRuleMatchDetails()) {
            System.out.println("Rule " + detail.getRule().getId()
                    + " matched=" + detail.isMatched()
                    + " (" + detail.getRule().getDescription() + ")");
            for (ConditionMatch cm : detail.getConditionMatches()) {
                System.out.println("    - " + cm.getField()
                        + " | actual=" + cm.getActualValue()
                        + " | op=" + cm.getOp()
                        + " | expected=" + cm.getExpectedValue()
                        + " | matched=" + cm.isMatched());
            }
            System.out.println();
        }
    }
}



