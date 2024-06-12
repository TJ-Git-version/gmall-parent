package com.atguigu.gmall.rabbit.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.rabbit.constant.MqConst;
import com.atguigu.gmall.rabbit.model.GmallCorrelationData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 统一封装RabbitMQ的发送消息方法
 */
@Service
@Slf4j
@SuppressWarnings("all")
public class RabbitService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 使用延迟队列发送消息
     * @param exchange：交换机名称
     * @param routingKey：路由key
     * @param message：消息内容
     * @param delayTime：延迟时间，单位：毫秒
     * @return
     */
    public boolean sendDelayMsg(String exchange, String routingKey, Object message, int delayTime) {
        try {
            // 封装GmallCorrelationData对象
            GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();
            String correlationDataId = getCorrelationDataId();
            gmallCorrelationData.setId(correlationDataId);
            gmallCorrelationData.setExchange(exchange);
            gmallCorrelationData.setRoutingKey(routingKey);
            gmallCorrelationData.setMessage(message);
            gmallCorrelationData.setDelay(true);
            gmallCorrelationData.setDelayTime(delayTime);
            // 将GmallCorrelationData对象存储到redis中，后面重试时使用
            redisTemplate.opsForValue().set(correlationDataId, JSON.toJSONString(gmallCorrelationData));
            // 发送消息
            rabbitTemplate.convertAndSend(exchange, routingKey, message, MqConst.getMessagePostProcessor(delayTime), gmallCorrelationData);
            return true;
        } catch (AmqpException e) {
            log.error("延迟消息发送失败：" + e.getMessage());
            return false;
        }
    }

    /**
     * 发送消息
     * @param exchange：交换机名称
     * @param routingKey：路由key
     * @param message：消息内容
     * @return
     */
    public boolean sendMsg(String exchange, String routingKey, Object message) {
        try {
            // 将gmalCorrelationData对象储存到redis中，用于后续的消息确认
            GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();
            // 生成唯一id
            String correlationDataId = getCorrelationDataId();
            gmallCorrelationData.setId(correlationDataId);
            gmallCorrelationData.setExchange(exchange);
            gmallCorrelationData.setRoutingKey(routingKey);
            gmallCorrelationData.setMessage(message);

            // 储存到redis中
            redisTemplate.opsForValue().set(correlationDataId, JSON.toJSONString(gmallCorrelationData), 10, TimeUnit.MINUTES);

            // 发送消息
            rabbitTemplate.convertAndSend(exchange, routingKey, message, gmallCorrelationData);
            return true;
        } catch (AmqpException e) {
            log.error("消息发送失败：" + e.getMessage());
            return false;
        }
    }

    /**
     * 获取CorrelationDataId
     * @return
     */
    private static String getCorrelationDataId() {
        return UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis();
    }

}
