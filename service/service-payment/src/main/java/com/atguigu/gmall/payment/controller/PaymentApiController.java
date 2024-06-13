package com.atguigu.gmall.payment.controller;

import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.payment.service.PaymentManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/api/payment")
@SuppressWarnings("all")
@RequiredArgsConstructor
public class PaymentApiController {

    private final PaymentManageService paymentManageService;

    /**
     * 支付宝支付接口
     * @param orderId
     * @return
     */
    @GetMapping("/alipay/submit/{orderId}")
    @ResponseBody
    public String alipaySubmit(@PathVariable Long orderId, HttpServletRequest request) {
        // 调用支付宝支付接口，生成支付链接 并返回给前端
        String userId = AuthContextHolder.getUserId(request);
        return paymentManageService.paymentSubmit(orderId, PaymentType.ALIPAY.name(), userId);
    }

}
