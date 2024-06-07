package com.atguigu.gmall.mq.provider;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.rabbit.service.RabbitService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mq")
@SuppressWarnings("all")
@RequiredArgsConstructor
public class MqProvider {

    private final RabbitService rabbitService;

    @GetMapping("/send")
    public Result<Void> send(){
        String msg = "你好，今天天气真好！";
        rabbitService.sendMsg("exchange.confirm", "routingKey.confirm2", msg);
        return Result.ok();
    }

}
