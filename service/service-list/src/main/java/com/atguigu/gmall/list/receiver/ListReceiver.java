package com.atguigu.gmall.list.receiver;

import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.rabbit.constant.MqConst;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.impl.AMQImpl;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class ListReceiver {

    private final SearchService searchService;

    /**
     * 商品上架
     */
    @SneakyThrows
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(value = MqConst.QUEUE_GOODS_UPPER, durable = "true", autoDelete = "false"),
                    exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS),
                    key = {MqConst.ROUTING_GOODS_UPPER}
            )
    })
    public void upperGoodsToEs(Long skuId, Message message, Channel channel) {
        // 更新es中商品的状态信息
        Goods goods = searchService.upperGoods(skuId);
        if (Objects.isNull(goods)) {
            //  写入日志或将这条消息写入数据库，短信接口通知管理员
            log.error("商品上架失败，skuId：{}", skuId);
            throw new GmallException("商品上架失败");
        }
        // 消息确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    /**
     * 商品下架
     */
    @SneakyThrows
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(
                            value = MqConst.QUEUE_GOODS_LOWER,
                            durable = "true",
                            autoDelete = "false"
                    ),
                    exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_GOODS),
                    key = {MqConst.ROUTING_GOODS_LOWER}
            )
    })
    public void lowerGoodsToEs(Long skuId, Message message, Channel channel) {
        // 更新es中商品的状态信息
        Boolean flag = searchService.lowerGoods(skuId);
        if (!flag) {
            //  写入日志或将这条消息写入数据库，短信接口通知管理员
            log.error("商品下架失败，skuId：{}", skuId);
            throw new GmallException("商品下架失败，请检查商品是否存在");
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
