package com.atguigu.gmall.weball.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Objects;

@Controller
@SuppressWarnings("all")
public class PayController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    @GetMapping("/pay.html")
    public String pay(Long orderId, Model model){
        Result<OrderInfo> result = orderFeignClient.getOrderInfoById(orderId);
        if (Objects.nonNull(result)) {
            model.addAttribute("orderInfo", result.getData());
        }
        return "payment/pay";
    }

}
