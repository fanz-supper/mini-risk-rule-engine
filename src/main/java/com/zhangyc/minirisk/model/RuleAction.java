package com.zhangyc.minirisk.model;

public enum RuleAction {
    /**
     * 允许通过
     */
    ALLOW,

    /**
     * 拒绝（强拦截）
     */
    REJECT,

    /**
     * 人工审核
     */
    MANUAL_REVIEW
}
