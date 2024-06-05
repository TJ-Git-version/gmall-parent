package com.atguigu.gmall.weball.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PayController {

    @GetMapping("/pay.html")
    public String pay(Long orderId){
        return "payment/pay";
    }

}
