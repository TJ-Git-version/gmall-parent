package com.atguigu.gmall.payment.controller;

import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.payment.service.PaymentManageService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("/api/payment")
@SuppressWarnings("all")
@RequiredArgsConstructor
public class PaymentApiController {

    private final PaymentManageService paymentManageService;

    /**
     * 支付宝退款接口
     * @param paramsMap
     * @return
     */
    @GetMapping("/alipay/refund/{orderId}")
    @ApiOperation("支付宝退款接口")
    public String alipayRefund(@PathVariable Long orderId) {
        boolean flag = paymentManageService.alipayRefund(orderId);
        if (flag) {
            return "redirect:" + "http://payment.gmall.com/pay/refundSuccess.html";
        } else {
            return "redirect:" + "http://payment.gmall.com/pay/refundFail.html";
        }
    }
    /**
     * 支付宝支付异步回调接口
     * @param paramsMap
     * @return
     */
    @PostMapping("/alipay/callback/notify")
    @ResponseBody
    @ApiOperation("支付宝支付异步回调接口")
    public String alipayCallbackNotify(@RequestParam Map<String, String> paramsMap) {
        return paymentManageService.alipayCallbackNotify(paramsMap);
    }

    /**
     * 支付宝支付同步回调接口
     * @return
     */
    @GetMapping("/alipay/callback/return")
    @ApiOperation("支付宝支付同步回调接口")
    public String alipayCallbackReturn() {
        return paymentManageService.alipayCallback();
    }
    /**
     * 支付宝支付接口
     * @param orderId
     * @return
     */
    @GetMapping("/alipay/submit/{orderId}")
    @ResponseBody
    @ApiOperation("支付宝支付接口")
    public String alipaySubmit(@PathVariable Long orderId, HttpServletRequest request) {
        // 调用支付宝支付接口，生成支付链接 并返回给前端
        String userId = AuthContextHolder.getUserId(request);
        return paymentManageService.paymentSubmit(orderId, PaymentType.ALIPAY.name(), userId);
    }

}
