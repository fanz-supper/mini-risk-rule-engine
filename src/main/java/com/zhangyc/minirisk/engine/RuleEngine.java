package com.zhangyc.minirisk.engine;

import com.zhangyc.minirisk.model.DecisionResult;
import com.zhangyc.minirisk.model.RiskContext;
import com.zhangyc.minirisk.model.Rule;

import java.util.List;

/**
 * 风控规则引擎接口。
 */
public interface RuleEngine {

    /**
     * 对给定上下文执行规则集合，输出决策结果。
     */
    DecisionResult evaluate(RiskContext context, List<Rule> rules);
}