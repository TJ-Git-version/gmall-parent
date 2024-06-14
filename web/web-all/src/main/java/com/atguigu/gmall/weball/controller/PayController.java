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

    /**
     * 退款失败页面
     *
     * @param orderId
     * @param model
     * @return
     */
    @GetMapping("/pay/refundFail.html")
    public String refundFail() {
        return "payment/refundFail";
    }

    /**
     * 退款成功页面
     *
     * @param orderId
     * @param model
     * @return
     */
    @GetMapping("/pay/refundSuccess.html")
    public String refundSuccess() {
        return "payment/refundSuccess";
    }

    /**
     * 支付成功页面
     *
     * @param orderId
     * @param model
     * @return
     */
    @GetMapping("/pay/success.html")
    public String paySuccess() {
        return "payment/success";
    }

    @GetMapping("/pay.html")
    public String pay(Long orderId, Model model) {
        OrderInfo orderInfo = orderFeignClient.getOrderInfoById(orderId);
        if (Objects.nonNull(orderInfo)) {
            model.addAttribute("orderInfo", orderInfo);
        }
        return "payment/pay";
    }

}
