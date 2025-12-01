package com.zhangyc.minirisk.model;

/**
 * 描述某条规则中的「一个条件」在某次风控决策中的评估情况。
 */
public class ConditionMatch {

    /** 配置中的字段名，例如 "order.amount" */
    private final String field;

    /** 操作符，例如 ">", "<", "==", "!=" 等 */
    private final String op;

    /** 配置中的期望值（字符串形式），例如 "1000"、"true" 等 */
    private final String expectedValue;

    /** 实际值（转换为字符串方便展示日志），例如 "1500"、"false" 等 */
    private final String actualValue;

    /** 本次条件是否成立 */
    private final boolean matched;

    public ConditionMatch(String field,
                          String op,
                          String expectedValue,
                          String actualValue,
                          boolean matched) {
        this.field = field;
        this.op = op;
        this.expectedValue = expectedValue;
        this.actualValue = actualValue;
        this.matched = matched;
    }

    public String getField() {
        return field;
    }

    public String getOp() {
        return op;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public String getActualValue() {
        return actualValue;
    }

    public boolean isMatched() {
        return matched;
    }

    @Override
    public String toString() {
        return "ConditionMatch{" +
                "field='" + field + '\'' +
                ", op='" + op + '\'' +
                ", expectedValue='" + expectedValue + '\'' +
                ", actualValue='" + actualValue + '\'' +
                ", matched=" + matched +
                '}';
    }
}