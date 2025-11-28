package com.zhangyc.minirisk.model;

import java.util.Objects;

/**
 * 风控决策时的上下文数据。
 */
public class RiskContext {

    // 用户相关
    private String userId;
    private boolean newUser;
    private int registerMinutes; // 注册到现在的分钟数
    private int historyOrderCount;

    // 订单相关
    private String orderId;
    private double orderAmount;

    // 设备 & IP 相关
    private String deviceId;
    private int deviceLoginUserCountIn10Min;
    private String ip;
    private boolean ipInBlacklist;

    // ======== 链式 set 方法，使用起来更方便 ========

    public String getUserId() {
        return userId;
    }

    public RiskContext setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public boolean isNewUser() {
        return newUser;
    }

    public RiskContext setNewUser(boolean newUser) {
        this.newUser = newUser;
        return this;
    }

    public int getRegisterMinutes() {
        return registerMinutes;
    }

    public RiskContext setRegisterMinutes(int registerMinutes) {
        this.registerMinutes = registerMinutes;
        return this;
    }

    public int getHistoryOrderCount() {
        return historyOrderCount;
    }

    public RiskContext setHistoryOrderCount(int historyOrderCount) {
        this.historyOrderCount = historyOrderCount;
        return this;
    }

    public String getOrderId() {
        return orderId;
    }

    public RiskContext setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public double getOrderAmount() {
        return orderAmount;
    }

    public RiskContext setOrderAmount(double orderAmount) {
        this.orderAmount = orderAmount;
        return this;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public RiskContext setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public int getDeviceLoginUserCountIn10Min() {
        return deviceLoginUserCountIn10Min;
    }

    public RiskContext setDeviceLoginUserCountIn10Min(int count) {
        this.deviceLoginUserCountIn10Min = count;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public RiskContext setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public boolean isIpInBlacklist() {
        return ipInBlacklist;
    }

    public RiskContext setIpInBlacklist(boolean ipInBlacklist) {
        this.ipInBlacklist = ipInBlacklist;
        return this;
    }

    @Override
    public String toString() {
        return "RiskContext{" +
                "userId='" + userId + '\'' +
                ", newUser=" + newUser +
                ", registerMinutes=" + registerMinutes +
                ", historyOrderCount=" + historyOrderCount +
                ", orderId='" + orderId + '\'' +
                ", orderAmount=" + orderAmount +
                ", deviceId='" + deviceId + '\'' +
                ", deviceLoginUserCountIn10Min=" + deviceLoginUserCountIn10Min +
                ", ip='" + ip + '\'' +
                ", ipInBlacklist=" + ipInBlacklist +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RiskContext)) return false;
        RiskContext that = (RiskContext) o;
        return newUser == that.newUser &&
                registerMinutes == that.registerMinutes &&
                historyOrderCount == that.historyOrderCount &&
                Double.compare(that.orderAmount, orderAmount) == 0 &&
                deviceLoginUserCountIn10Min == that.deviceLoginUserCountIn10Min &&
                ipInBlacklist == that.ipInBlacklist &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(orderId, that.orderId) &&
                Objects.equals(deviceId, that.deviceId) &&
                Objects.equals(ip, that.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, newUser, registerMinutes, historyOrderCount,
                orderId, orderAmount, deviceId, deviceLoginUserCountIn10Min, ip, ipInBlacklist);
    }
}

