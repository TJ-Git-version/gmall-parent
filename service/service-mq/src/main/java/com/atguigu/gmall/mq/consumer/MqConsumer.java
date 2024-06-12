package com.atguigu.gmall.mq.consumer;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@SuppressWarnings("all")
public class MqConsumer {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 延迟消费者
     *
     * @param msg
     */
    @SneakyThrows
    @RabbitListener(queues = DelayedMqConfig.queue_delay_1)
    public void delayedConsumer(String msg, Message message, Channel channel) {
        // 解决消息重复消费问题，设置消息的唯一标识
        Boolean flag = redisTemplate.opsForValue().setIfAbsent("delayedConsumer:" + msg, 0, 10, TimeUnit.MINUTES);
        // 如果标识为false且值为1，说明消息已经被消费过，则不再消费
        if (!flag) {
            Integer result = (Integer) redisTemplate.opsForValue().get("delayedConsumer:" + msg);
            if (result != null && result == 1) {
                // 消息已被消费，拒绝消费
                System.err.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                System.err.println("消息已被消费，拒绝消费：" + msg);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        } else { // 标识为true，设置修复状态为1
            redisTemplate.opsForValue().set("delayedConsumer:" + msg, 1);
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            System.out.println("收到延迟消息：" + msg);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    /**
     * 死讯消费者
     *
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
     *
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
