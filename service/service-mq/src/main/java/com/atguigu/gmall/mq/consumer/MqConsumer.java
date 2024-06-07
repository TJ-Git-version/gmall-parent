package com.atguigu.gmall.mq.consumer;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MqConsumer {


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
