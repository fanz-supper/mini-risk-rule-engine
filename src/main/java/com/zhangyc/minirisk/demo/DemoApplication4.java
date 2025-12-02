package com.zhangyc.minirisk.demo;

import com.zhangyc.minirisk.engine.ExplainableRuleEngine;
import com.zhangyc.minirisk.engine.RuleEngine;
import com.zhangyc.minirisk.engine.SimpleRuleEngine;
import com.zhangyc.minirisk.model.*;
import com.zhangyc.minirisk.registry.RuleRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * v0.4 Demo：添加日志
 */
@Slf4j
public class DemoApplication4 {
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

        // ========== 用日志记录 ==========

        log.info("Scene: {}", scene);
        log.info("Context: {}", ctx);
        log.info("Final Decision: {}", result.getFinalAction());

        log.info("Matched rules:");
        for (Rule rule : result.getMatchedRules()) {
            log.info("  - {} | {} | action={} | priority={} | scene={}",
                    rule.getId(),
                    rule.getDescription(),
                    rule.getAction(),
                    rule.getPriority(),
                    rule.getScene());
        }

        log.info("=== Explanation Details ===");
        for (RuleMatchDetail detail : result.getRuleMatchDetails()) {
            log.info("Rule {} matched={} ({})",
                    detail.getRule().getId(),
                    detail.isMatched(),
                    detail.getRule().getDescription());
            for (ConditionMatch cm : detail.getConditionMatches()) {
                log.info("    - {} | actual={} | op={} | expected={} | matched={}",
                        cm.getField(),
                        cm.getActualValue(),
                        cm.getOp(),
                        cm.getExpectedValue(),
                        cm.isMatched());
            }
        }
    }
}



