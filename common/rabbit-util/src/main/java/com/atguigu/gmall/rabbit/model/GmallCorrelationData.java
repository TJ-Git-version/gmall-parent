package com.atguigu.gmall.rabbit.model;

import org.springframework.amqp.rabbit.connection.CorrelationData;

import java.util.List;

public class GmallCorrelationData extends CorrelationData {

    /**
     * 交换机名
     */
    private String exchange;
    /**
     * 路由键
     */
    private String routingKey;
    /**
     * 消息体
     */
    private Object message;
    /**
     * 重试次数
     */
    private int retryCount = 0;
    /**
     * 消息类型  是否是延迟消息
     *  true  是
     *  false 不是
     */
    private boolean isDelay = false;
    /**
     * 延迟时间：单位：秒
     */
    private int delayTime = 10;

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public boolean isDelay() {
        return isDelay;
    }

    public void setDelay(boolean delay) {
        isDelay = delay;
    }

    public int getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }
}
