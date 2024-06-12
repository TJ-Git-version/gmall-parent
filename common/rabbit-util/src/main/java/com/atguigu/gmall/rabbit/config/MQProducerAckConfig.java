package com.atguigu.gmall.rabbit.config;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.rabbit.constant.MqConst;
import com.atguigu.gmall.rabbit.model.GmallCorrelationData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * RabbitMQ消息确认机制配置类
 * ConfirmCallback：消息发送到交换机后，交换机把消息路由到队列，如果队列消费者接收到消息，则会调用该方法，并传入CorrelationData对象，
 * 该对象包含了消息的唯一标识id，可以用来标识消息是否到达队列。
 * <p>
 * 如果发送失败，生产者进行重试，重试3次后仍然失败，则会调用nack方法，该方法传入CorrelationData对象，
 * <p>
 * ReturnCallback：如果消息没有到达队列，则会调用该方法，并传入消息对象、回复码、回复文本、交换机、路由键等信息。
 * <p>
 * 该类主要是配置rabbitTemplate的confirmCallback和returnCallback，并实现confirm和return方法。
 */
@Component
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("all")
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    private final RabbitTemplate rabbitTemplate;

    private final RedisTemplate redisTemplate;

    /**
     * 绑定到rabbitMQ的exchange和queue中，并初始化rabbitTemplate的confirmCallback和returnCallback
     *
     * @PostConstruct：在Bean实例化完成后，执行该方法
     */
    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }

    /**
     * 信息发送到交换机后，交换机把消息路由到队列，如果队列消费者接收到消息，则会调用该方法，并传入CorrelationData对象，
     * 该对象包含了消息的唯一标识id，可以用来标识消息是否到达队列。
     * <p>
     * 如果发送失败，生产者进行重试，重试3次后仍然失败，则会调用nack方法，该方法传入CorrelationData对象，
     *
     * @param correlationData 一个包含消息唯一标识id的对象
     * @param ack             交换机是否成功接收到消息，true表示成功，false表示失败
     * @param cause           失败原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            log.info("消息发送到交换机成功：{}", correlationData.getId());
        } else {
            log.error("消息发送到交换机失败：", cause);
            this.retrySendMsg(correlationData);
        }
    }

    /**
     * 重试机制方法：如果消息发送失败，则进行重试，最多重试3次
     *
     * @param correlationData
     */
    private void retrySendMsg(CorrelationData correlationData) {
        if (correlationData == null) {
            log.error("消息没有到达队列，未找到CorrelationData对象");
            return;
        }
        // 将CorrelationData对象转换为GmallCorrelationData对象
        GmallCorrelationData gmallCorrelationData = (GmallCorrelationData) correlationData;
        if (gmallCorrelationData == null) {
            log.error("消息发送到队列失败，未找到CorrelationData对象：{}" + correlationData.getId());
            return;
        }
        // 重试次数
        int retryCount = gmallCorrelationData.getRetryCount();
        if (retryCount >= 3) {
            log.error("消息发送到队列失败，超过重试次数，放弃发送：{}" + correlationData.getId());
        } else {
            retryCount++;
            gmallCorrelationData.setRetryCount(retryCount);
            // 更新redis中的自定义的GmallCorrelationData中的重试次数
            redisTemplate.opsForValue().set(correlationData.getId(), JSON.toJSONString(gmallCorrelationData), 10, TimeUnit.MINUTES);
            if (gmallCorrelationData.isDelay()) {
                // 延迟发送消息
                rabbitTemplate.convertAndSend(gmallCorrelationData.getExchange(), gmallCorrelationData.getRoutingKey(), gmallCorrelationData.getMessage(), MqConst.getMessagePostProcessor(gmallCorrelationData.getDelayTime()), gmallCorrelationData);
            } else {
                // 立即发送消息
                rabbitTemplate.convertAndSend(gmallCorrelationData.getExchange(), gmallCorrelationData.getRoutingKey(), gmallCorrelationData.getMessage(), gmallCorrelationData);
            }
            // 打印日志
            log.error("消息发送到队列失败，进行第{}次重试：{}", retryCount, correlationData.getId());
        }
    }

    /**
     * 交换机给消息没有到达队列时，会调用该方法，并传入消息对象、回复码、回复文本、交换机、路由键等信息。
     *
     * @param message    交换机没有到达队列的消息对象
     * @param replyCode  回复码，一般是0
     * @param replyText  回复文本，一般是"NO_ROUTE"
     * @param exchange   交换机名称
     * @param routingKey 路由键
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.out.println("消息没有到达队列：" + message.toString());
        System.out.println("replyCode：" + replyCode);
        System.out.println("replyText：" + replyText);
        System.out.println("exchange：" + exchange);
        System.out.println("routingKey：" + routingKey);

        String correlationId = message.getMessageProperties().getCorrelationId();
        if (StringUtils.isEmpty(correlationId)) {
            correlationId = (String) message.getMessageProperties().getHeaders().get("spring_returned_message_correlation");
        }
        log.info("message中获取的correlationId：" + correlationId);
        if (!StringUtils.isEmpty(correlationId)) {
            // 从redis中获取自定义的GmallCorrelationData对象，里面包含了消息的重试次数
            String correlationDataJson = (String) redisTemplate.opsForValue().get(correlationId);
            GmallCorrelationData gmallCorrelationData = JSON.parseObject(correlationDataJson, GmallCorrelationData.class);
            log.info("从redis中获取的GmallCorrelationData对象：" + gmallCorrelationData);

            // 进行重试
            this.retrySendMsg(gmallCorrelationData);
        }
    }
    /*
    消息没有到达队列：(
    Body:'你好，今天天气真好！'
            MessageProperties
                [
                    headers={
                        b3=ca29c72761755b2b-ca29c72761755b2b-0,
                        spring_returned_message_correlation=46016be7f067437aaa71685d8eaefa401717745001137
                        },
                        contentType=text/plain,
                        contentEncoding=UTF-8,
                        contentLength=0,
                        receivedDeliveryMode=PERSISTENT,
                        priority=0, deliveryTag=0
                 ]
    )
    replyCode：312
    replyText：NO_ROUTE
    exchange：exchange.confirm
    routingKey：routingKey.confirm2
     */
}
