package com.atguigu.gmall.rabbit.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.rabbit.model.GmallCorrelationData;
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
@SuppressWarnings("all")
public class RabbitService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送消息
     * @param exchange：交换机名称
     * @param routingKey：路由key
     * @param message：消息内容
     * @return
     */
    public boolean sendMsg(String exchange, String routingKey, Object message) {
        // 将gmalCorrelationData对象储存到redis中，用于后续的消息确认
        GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();
        // 生成唯一id
        String correlationDataId = UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis();
        gmallCorrelationData.setId(correlationDataId);
        gmallCorrelationData.setExchange(exchange);
        gmallCorrelationData.setRoutingKey(routingKey);
        gmallCorrelationData.setMessage(message);

        // 储存到redis中
        redisTemplate.opsForValue().set(correlationDataId, JSON.toJSONString(gmallCorrelationData), 10, TimeUnit.MINUTES);

        // 发送消息
        rabbitTemplate.convertAndSend(exchange, routingKey, message, gmallCorrelationData);
        return true;
    }

}
