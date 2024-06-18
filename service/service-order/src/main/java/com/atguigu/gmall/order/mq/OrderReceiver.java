package com.atguigu.gmall.order.mq;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderManagerService;
import com.atguigu.gmall.rabbit.constant.MqConst;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@SuppressWarnings("all")
public class OrderReceiver {

    @Autowired
    private OrderManagerService orderManagerService;

    @Autowired
    private RedisTemplate redisTemplate;

    public static void main(String[] args) {
    }

    /**
     * 退款订单，关闭订单
     *
     * @param orderId
     */
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER_CANCEL, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_PAYMENT_CLOSE, durable = "true", type = "direct", autoDelete = "false"),
            key = MqConst.ROUTING_PAYMENT_CLOSE
    ))
    public void refundOrder(Long orderId, Message message, Channel channel) {
        // 防止重复提交
        String checkRepeatKey = getRedisRepeatKey("refundOrder", orderId);
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(checkRepeatKey, "0", 10, TimeUnit.MINUTES);
        if (!flag) {
            if (Objects.equals(redisTemplate.opsForValue().get(checkRepeatKey), "1")) {
                // 如果已消费，直接抛弃队列信息
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        } else {
            if (orderId != null && orderId != 0L) {
                // 修改订单状态
                orderManagerService.updateOrderStatus(orderId, ProcessStatus.CLOSED);
                // 恢复库存数量

                // 手动确认
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }

        }
    }

    /**
     * 支付成功后更新订单状态
     *
     * @param orderMap
     * @param message
     * @param channel
     */
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_PAYMENT_PAY, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_PAYMENT_PAY, durable = "true", type = "direct", autoDelete = "false"),
            key = MqConst.ROUTING_PAYMENT_PAY
    ))
    public void payOrder(Long orderId, Message message, Channel channel) {
        // 防止重复提交
        String checkRepeatKey = getRedisRepeatKey("orderPay", orderId);
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(checkRepeatKey, "0", 10, TimeUnit.MINUTES);
        if (!flag && Objects.equals(redisTemplate.opsForValue().get(checkRepeatKey), "1")) {
            // 如果已消费，直接抛弃队列信息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } else {
            if (orderId != null && orderId != 0L) {
                OrderInfo orderInfo = orderManagerService.getOrderInfoById(orderId);
                // 幂等性校验，只有订单处于未支付状态才能修改
                if (Objects.nonNull(orderInfo) && OrderStatus.UNPAID.name().equals(orderInfo.getOrderStatus())) {
                    // 更新订单状态，已支付
                    orderManagerService.updateOrderStatus(orderId, ProcessStatus.PAID);
                    // 减库存
                    orderManagerService.sendMqReduceStock(orderInfo);
                    // 修改当前修改订单状态为已支付
                    redisTemplate.opsForValue().set(checkRepeatKey, "1", 10, TimeUnit.MINUTES);
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                }
            }
        }

    }

    /**
     * 取消订单
     *
     * @param orderId
     * @param message
     * @param channel
     */
    @RabbitListener(queues = MqConst.QUEUE_ORDER_CANCEL)
    public void cancelOrder(Long orderId, Message message, Channel channel) {
        try {
            // 解决分布式下取消订单状态幂等性
            Boolean flag = redisTemplate
                    .opsForValue()
                    .setIfAbsent(getRedisRepeatKey(RedisConst.ORDER_CANCEL_PREFIX, orderId), "0", 5, TimeUnit.MINUTES);
            if (!flag) {
                // 消息已被消费，直接丢弃
                String result = (String) redisTemplate.opsForValue().get(getRedisRepeatKey(RedisConst.ORDER_CANCEL_PREFIX, orderId));
                if (StringUtils.isNotBlank(result) && result.equals("1")) {
                    log.info("消息已被消费，直接丢弃，订单id：{}", orderId);
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                }
            } else {
                if (orderId != null) {
                    // 根据订单id、支付状态和支付流程状态去查询订单
                    OrderInfo orderInfo = orderManagerService.getOne(
                            Wrappers.<OrderInfo>lambdaQuery()
                                    .eq(OrderInfo::getId, orderId)
                                    .eq(OrderInfo::getOrderStatus, OrderStatus.UNPAID.name())
                                    .eq(OrderInfo::getProcessStatus, ProcessStatus.UNPAID.name())
                    );
                    // 如果订单不为空，则取消订单
                    if (orderInfo != null) {
                        orderManagerService.cancelOrderStatus(orderInfo);
                    }
                    log.info("订单{}取消成功", orderId);
                    redisTemplate.opsForValue().set(getRedisRepeatKey(RedisConst.ORDER_CANCEL_PREFIX, orderId), "1");
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                }
            }
        } catch (IOException e) {
            log.error("订单取消失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取取消订单的redis key
     *
     * @param orderId
     * @return
     */
    private String getRedisRepeatKey(String prefix, Long orderId) {
        return prefix + orderId;
    }

}
