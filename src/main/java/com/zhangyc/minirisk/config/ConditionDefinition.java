package com.zhangyc.minirisk.config;

/**
 * 单个条件的配置定义，对应 JSON 里的一个条件：
 * 例如 field = "order.amount", op = ">", value = "1000"
 */
public class ConditionDefinition {

    private String field;
    private String op;
    private String value;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

