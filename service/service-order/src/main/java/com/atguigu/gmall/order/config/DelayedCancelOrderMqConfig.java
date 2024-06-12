package com.atguigu.gmall.order.config;

import com.atguigu.gmall.rabbit.constant.MqConst;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DelayedCancelOrderMqConfig {

    /**
     * 创建延迟队列
     */
    @Bean
    public Queue cancelOrderQueue() {
        return new Queue(MqConst.QUEUE_ORDER_CANCEL, true, false, false, null);
    }

    /**
     * 创建延迟交换机
     * CustomExchange参数说明：
     * 1. name：交换机名称
     * 2. type：交换机类型： x-delayed-message，表示该交换机类型为延迟交换机
     * 3. durable：是否持久化
     * 4. autoDelete：是否自动删除
     * 5. arguments：扩展参数：x-delayed-type=direct，表示该交换机类型为延迟交换机
     */
    @Bean
    public CustomExchange cancelOrderExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(
                MqConst.EXCHANGE_DIRECT_ORDER_CANCEL,
                "x-delayed-message",
                true,
                false,
                args
        );
    }

    /**
     * 绑定队列到延迟交换机，并设置路由键
     */
    @Bean
    public Binding cancelOrderBinding() {
        return BindingBuilder
                .bind(cancelOrderQueue())
                .to(cancelOrderExchange())
                .with(MqConst.ROUTING_ORDER_CANCEL)
                .noargs();
    }


}
