package com.zhangyc.minirisk.demo;

import com.zhangyc.minirisk.engine.RuleEngine;
import com.zhangyc.minirisk.engine.SimpleRuleEngine;
import com.zhangyc.minirisk.model.*;

import java.util.Arrays;
import java.util.List;

/**
 * v0.1 Demo：在内存中硬编码几条规则，跑一下看看引擎效果。
 */
public class DemoApplication {

    public static void main(String[] args) {
        // 1. 准备几条硬编码规则
        List<Rule> rules = buildHardcodedRules();

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

    private static List<Rule> buildHardcodedRules() {
        // 规则1：设备登录爆发，拒绝
        Rule rLogin = new Rule(
                "R_LOGIN_001",
                "同一设备10分钟内登录账号数过多，疑似撞库",
                "LOGIN",
                100,
                ctx -> ctx.getDeviceLoginUserCountIn10Min() > 5,
                RuleAction.REJECT
        );

        // 规则2：新用户首单金额过高且注册时间很短，人工审核
        Rule rPayNewUserHighAmount = new Rule(
                "R_PAY_001",
                "新用户首单金额过高，且注册时间过短",
                "PAY",
                90,
                ctx -> ctx.isNewUser()
                        && ctx.getHistoryOrderCount() == 0
                        && ctx.getOrderAmount() > 1000
                        && ctx.getRegisterMinutes() < 60,
                RuleAction.MANUAL_REVIEW
        );

        // 规则3：IP 在黑名单，拒绝
        Rule rIpBlacklist = new Rule(
                "R_IP_001",
                "IP 命中黑名单",
                "COMMON",
                200,
                RiskContext::isIpInBlacklist,
                RuleAction.REJECT
        );

        return Arrays.asList(rLogin, rPayNewUserHighAmount, rIpBlacklist);
    }
}

