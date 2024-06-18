package com.atguigu.gmall.order.mq;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.order.service.OrderManagerService;
import com.atguigu.gmall.rabbit.constant.MqConst;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 *
 */
@Component
@Slf4j
@SuppressWarnings("all")
public class OrderConsumer {

    @Autowired
    private OrderManagerService orderManagerService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 仓库已减库存，修改订单状态为：待发货状态
     * @param stockJson
     * @param message
     * @param channel
     */
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_WARE_ORDER, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_WARE_ORDER, durable = "true", type = "direct", autoDelete = "false"),
            key = MqConst.ROUTING_WARE_ORDER
    ))
    public void stockOrderStatus(String stockJson, Message message, Channel channel) {
        Map<String, String> stockMap = JSON.parseObject(stockJson, Map.class);
        if (stockMap != null) {
            String orderId = stockMap.get("orderId");
            String status = stockMap.get("status");
            if ("DEDUCTED".equals(status)) { //已减库存
                orderManagerService.updateOrderStatus(Long.valueOf(orderId), ProcessStatus.WAITING_DELEVER);
            } else { //库存超卖
                // 通知管理员进行修复
                log.info("库存超卖，订单号：{}" , orderId);
            }
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
