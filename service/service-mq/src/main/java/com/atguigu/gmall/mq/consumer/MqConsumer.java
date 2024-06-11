package com.atguigu.gmall.mq.consumer;

import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class MqConsumer {

    /**
     * 延迟消费者
     * @param msg
     */
    @SneakyThrows
    @RabbitListener(queues = DelayedMqConfig.queue_delay_1)
    public void delayedConsumer(String msg, Message message, Channel channel) {
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        System.out.println("收到延迟消息：" + msg);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    /**
     * 死讯消费者
     * @param msg
     * @param message
     * @param channel
     */
    @SneakyThrows
    @RabbitListener(queues = DeadLetterMqConfig.queue_dead_2)
    public void deadLetterConsumer(String msg, Message message, Channel channel) {
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        System.out.println("收到死信消息：" + msg);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    /**
     * 正常消费者
     * @param message
     * @param channel
     */
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "queue.confirm", durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = "exchange.confirm", durable = "true", autoDelete = "false"),
            key = {"routingKey.confirm"}
    ))
    public void consume(Message message, Channel channel) {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        System.out.println("收到消息：" + new String(message.getBody()));
    }

}
