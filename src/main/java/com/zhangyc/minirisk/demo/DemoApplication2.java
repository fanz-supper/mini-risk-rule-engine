package com.zhangyc.minirisk.demo;

import com.zhangyc.minirisk.engine.RuleEngine;
import com.zhangyc.minirisk.engine.SimpleRuleEngine;
import com.zhangyc.minirisk.model.*;
import com.zhangyc.minirisk.registry.RuleRegistry;

import java.util.List;

/**
 * v0.2.1 Demo：按场景（PAY）从规则中心获取规则，执行风控决策。
 */
public class DemoApplication2 {

    public static void main(String[] args) {
        // 1. 声明当前是支付场景
        String scene = "PAY";

        // 2. 从规则注册中心按场景取规则（PAY + COMMON）
        List<Rule> rules = RuleRegistry.getRulesForScene(scene);

        // 3. 构造一个「可疑首单」上下文（和之前一样）
        RiskContext ctx = new RiskContext()
                .setUserId("U123")
                .setNewUser(true)
                .setRegisterMinutes(30)
                .setHistoryOrderCount(0)
                .setOrderId("O20250101")
                .setOrderAmount(1500.0)
                .setDeviceId("D001")
                .setDeviceLoginUserCountIn10Min(10)
                .setIp("9.9.9.9")
                .setIpInBlacklist(true);

        // 4. 调用引擎
        RuleEngine engine = new SimpleRuleEngine();
        DecisionResult result = engine.evaluate(ctx, rules);

        System.out.println("Scene: " + scene);
        System.out.println("Context: " + ctx);
        System.out.println("Decision: " + result.getFinalAction());
        System.out.println("Matched rules:");
        result.getMatchedRules().forEach(rule ->
                System.out.println("  - " + rule.getId() + " | " + rule.getDescription() +
                        " | action=" + rule.getAction() +
                        " | priority=" + rule.getPriority() +
                        " | scene=" + rule.getScene())
        );
    }
}


