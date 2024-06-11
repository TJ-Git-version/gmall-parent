package com.atguigu.gmall.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 延迟队列配置
 */
@Configuration
public class DelayedMqConfig {
    public static final String exchange_delay = "exchange.delay";
    public static final String routing_delay = "routing.delay";
    public static final String queue_delay_1 = "queue.delay.1";

    /**
     * 延迟队列1
     * @return
     */
    @Bean
    public Queue queueDelay1() {
        return new Queue(queue_delay_1, true);
    }

    /**
     * 自定义延迟交换机
     * @return
     */
    @Bean
    public CustomExchange customExchange(){
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(exchange_delay, "x-delayed-message", true, false, args);
    }

    /**
     * 绑定队列到延迟交换机
     */
    @Bean
    public Binding bindingDelay1() {
        return BindingBuilder.bind(queueDelay1()).to(customExchange()).with(routing_delay).noargs();
    }
}
