package com.zhangyc.minirisk.engine;

import com.zhangyc.minirisk.config.ConditionDefinition;
import com.zhangyc.minirisk.config.RuleConfigLoader;
import com.zhangyc.minirisk.config.RuleDefinition;
import com.zhangyc.minirisk.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.zhangyc.minirisk.config.RuleConfigLoader.*;

/**
 * 带「解释能力」的规则引擎：
 * - 内部委托一个基础 RuleEngine（例如 SimpleRuleEngine）做决策；
 * - 额外生成每条规则下各个条件的命中情况，填充到 DecisionResult.ruleMatchDetails 中。
 *
 * 这是一个典型的“装饰器”用法：在不修改原引擎逻辑的前提下，增加解释能力。
 */
public class ExplainableRuleEngine implements RuleEngine {

    private final RuleEngine delegate;

    public ExplainableRuleEngine(RuleEngine delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate engine must not be null");
    }

    @Override
    public DecisionResult evaluate(RiskContext context, List<Rule> rules) {
        // 1. 先用原有引擎算出基础结果（最终动作 + 命中规则列表）
        DecisionResult baseResult = delegate.evaluate(context, rules);

        // 2. 针对每条规则生成 RuleMatchDetail（这里只对命中规则生成解释）
        // 创建一个空的ArrayList用于存储规则匹配详情
        List<RuleMatchDetail> details = new ArrayList<>();
        // 遍历基础结果中所有命中的规则
        for (Rule rule : baseResult.getMatchedRules()) {
            // 根据规则ID从配置加载器中获取规则的定义详情
            RuleDefinition def = RuleConfigLoader.getRuleDefinitionById(rule.getId());
            if (def == null || def.getConditions() == null || def.getConditions().isEmpty()) {
                // 没有配置层定义，就简单记录一下命中
                details.add(new RuleMatchDetail(rule, true, new ArrayList<>()));
                continue;
            }

            // 创建一个空的ArrayList用于存储条件匹配详情
            List<ConditionMatch> conditionMatches = new ArrayList<>();
            // allTrue用于AND逻辑，代表所有条件都需满足；anyTrue用于OR逻辑，代表任意一个条件需满足
            boolean allTrue = true;
            boolean anyTrue = false;

            // 遍历规则定义中的所有条件
            for (ConditionDefinition c : def.getConditions()) {
                // 调用evaluateSingleCondition评估条件
                ConditionMatch cm = evaluateSingleCondition(context, c);
                // 将条件匹配结果添加到条件匹配详情列表中
                conditionMatches.add(cm);
                // 更新 allTrue 和 anyTrue 的值
                allTrue = allTrue && cm.isMatched();
                anyTrue = anyTrue || cm.isMatched();
            }

            // 声明变量ruleMatched存储规则是否匹配
            boolean ruleMatched;
            // 根据规则定义中的逻辑操作符进行逻辑判断
            String logicalOp = def.getLogicalOp();
            if (logicalOp == null || logicalOp.isEmpty() || "AND".equalsIgnoreCase(logicalOp)) {
                ruleMatched = allTrue;
            } else if ("OR".equalsIgnoreCase(logicalOp)) {
                ruleMatched = anyTrue;
            } else {
                // 未知逻辑操作符，按 AND 处理
                ruleMatched = allTrue;
            }

            // 一般来说 ruleMatched 应该和 baseResult 里的命中情况一致
            details.add(new RuleMatchDetail(rule, ruleMatched, conditionMatches));
        }

        // 3. 返回一个带有解释信息的新 DecisionResult
        return new DecisionResult(
                baseResult.getFinalAction(),
                baseResult.getMatchedRules(),
                details
        );
    }

    /**
     * 对单个 Condition 做评估，生成 ConditionMatch：
     * - 取出实际值 actualValue
     * - 使用 op 和 expectedValue 做比较
     */
    private ConditionMatch evaluateSingleCondition(RiskContext ctx, ConditionDefinition c) {
        String field = c.getField();
        String op = c.getOp();
        String expected = c.getValue();

        String actualStr;
        boolean matched;

        switch (field) {
            case "device.loginUserCountIn10Min": {
                int actual = ctx.getDeviceLoginUserCountIn10Min();
                int expectedInt = Integer.parseInt(expected);
                matched = compareInt(actual, op, expectedInt);
                actualStr = String.valueOf(actual);
                break;
            }
            case "user.isNew": {
                boolean actual = ctx.isNewUser();
                boolean expectedBool = Boolean.parseBoolean(expected);
                matched = compareBoolean(actual, op, expectedBool);
                actualStr = String.valueOf(actual);
                break;
            }
            case "user.historyOrderCount": {
                int actual = ctx.getHistoryOrderCount();
                int expectedInt = Integer.parseInt(expected);
                matched = compareInt(actual, op, expectedInt);
                actualStr = String.valueOf(actual);
                break;
            }
            case "order.amount": {
                double actual = ctx.getOrderAmount();
                double expectedDouble = Double.parseDouble(expected);
                matched = compareDouble(actual, op, expectedDouble);
                actualStr = String.valueOf(actual);
                break;
            }
            case "user.registerMinutes": {
                int actual = ctx.getRegisterMinutes();
                int expectedInt = Integer.parseInt(expected);
                matched = compareInt(actual, op, expectedInt);
                actualStr = String.valueOf(actual);
                break;
            }
            case "ip.inBlacklist": {
                boolean actual = ctx.isIpInBlacklist();
                boolean expectedBool = Boolean.parseBoolean(expected);
                matched = compareBoolean(actual, op, expectedBool);
                actualStr = String.valueOf(actual);
                break;
            }
            default: {
                // 未知字段：当作不匹配
                actualStr = "<unknown-field>";
                matched = false;
                break;
            }
        }

        return new ConditionMatch(field, op, expected, actualStr, matched);
    }
}

