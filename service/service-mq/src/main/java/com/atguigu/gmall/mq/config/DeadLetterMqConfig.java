package com.atguigu.gmall.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 死信队列配置
 */
@Configuration
@SuppressWarnings("all")
public class DeadLetterMqConfig {

    public static final String exchange_dead = "exchange.dead"; // 死信交换机
    public static final String routing_dead_1 = "routing.dead.1"; // 死信路由键1
    public static final String routing_dead_2 = "routing.dead.2"; // 死信路由键2
    public static final String queue_dead_1 = "queue.dead.1"; // 死信队列1
    public static final String queue_dead_2 = "queue.dead.2"; // 死信队列2

    /**
     * 创建交换机
     */
    @Bean
    public DirectExchange exchangeDead() {
        return new DirectExchange(exchange_dead, true, false, null);
    }

    /**
     * 创建死信队列1
     */
    @Bean
    public Queue queueDead1() {
        // 设置如果队列一 出现问题，则通过参数转到exchange_dead，routing_dead_2 上！
        Map<String, Object> arguments = new HashMap<>();
        // 设置过期时间
        arguments.put("x-message-ttl", 5 * 1000); // 5秒
        // 配置死讯队列参数
        arguments.put("x-dead-letter-exchange", exchange_dead); // 死信交换机
        arguments.put("x-dead-letter-routing-key", routing_dead_2); // 死信路由键
        return new Queue(queue_dead_1, true, false, false, arguments);
    }

    /**
     * 创建死信队列2
     */
    @Bean
    public Queue queueDead2() {
        return new Queue(queue_dead_2, true, false, false, null);
    }

    /**
     * 绑定死信队列1到死信交换机
     */
    @Bean
    public Binding bindDead1(Queue queueDead1, DirectExchange exchangeDead) {
        return BindingBuilder
                .bind(queueDead1)
                .to(exchangeDead)
                .with(routing_dead_1);
    }

    /**
     * 绑定死信队列2到死信交换机
     */
    @Bean
    public Binding bindDead2(Queue queueDead1, DirectExchange exchangeDead) {
        return BindingBuilder
                .bind(queueDead1)
                .to(exchangeDead)
                .with(routing_dead_2);
    }

}
