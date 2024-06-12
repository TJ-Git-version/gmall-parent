package com.atguigu.gmall.mq.provider;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import com.atguigu.gmall.rabbit.service.RabbitService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/mq")
@SuppressWarnings("all")
@RequiredArgsConstructor
public class MqProvider {

    private final RabbitService rabbitService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 延迟队列发送消息
     *
     * @return
     */
    @GetMapping("/sendDelayedMsg")
    public Result<Void> sendDelayedMsg() {
        // rabbitTemplate.convertAndSend(DelayedMqConfig.exchange_delay, DelayedMqConfig.routing_delay, "测试延迟消息", message -> {
        //     message.getMessageProperties().setDelay(5 * 1000);
        //     return message;
        // });
        rabbitService.sendDelayMsg(DelayedMqConfig.exchange_delay, DelayedMqConfig.routing_delay, "测试延迟消息", 5 * 1000);
        return Result.ok();
    }

    /**
     * 死信队列发送消息
     *
     * @return
     */
    @GetMapping("/deadSend")
    public Result<Void> deadSend() {
        rabbitService.sendMsg(DeadLetterMqConfig.exchange_dead, DeadLetterMqConfig.routing_dead_1, "死信测试消息");
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 发送死信测试消息");
        return Result.ok();
    }

    /**
     * 普通队列发送消息
     *
     * @return
     */
    @GetMapping("/send")
    public Result<Void> send() {
        String msg = "你好，今天天气真好！";
        rabbitService.sendMsg("exchange.confirm", "routingKey.confirm2", msg);
        return Result.ok();
    }

}
