package com.zhangyc.minirisk.demo;

import com.zhangyc.minirisk.config.RuleConfigLoader;
import com.zhangyc.minirisk.engine.RuleEngine;
import com.zhangyc.minirisk.engine.SimpleRuleEngine;
import com.zhangyc.minirisk.model.DecisionResult;
import com.zhangyc.minirisk.model.RiskContext;
import com.zhangyc.minirisk.model.Rule;
import com.zhangyc.minirisk.model.RuleAction;

import java.util.Arrays;
import java.util.List;

/**
 * v0.1 Demo：在内存中硬编码几条规则，跑一下看看引擎效果。
 */
public class DemoApplication1 {

    public static void main(String[] args) {
        // 1. 从配置文件里加载规则
        List<Rule> rules = RuleConfigLoader.loadRulesFromClasspath("rules-demo.json");

        // 2. 构造一个「可疑首单」上下文
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

        // 3. 调用引擎
        RuleEngine engine = new SimpleRuleEngine();
        DecisionResult result = engine.evaluate(ctx, rules);

        System.out.println("Context: " + ctx);
        System.out.println("Decision: " + result.getFinalAction());
        System.out.println("Matched rules:");
        result.getMatchedRules().forEach(rule ->
                System.out.println("  - " + rule.getId() + " | " + rule.getDescription() +
                        " | action=" + rule.getAction() +
                        " | priority=" + rule.getPriority()));
    }
}

