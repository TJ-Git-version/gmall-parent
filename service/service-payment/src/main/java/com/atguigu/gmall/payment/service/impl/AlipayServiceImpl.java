package com.atguigu.gmall.payment.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.*;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.constant.AlipayConstant;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentManageService;
import com.atguigu.gmall.rabbit.constant.MqConst;
import com.atguigu.gmall.rabbit.service.RabbitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@SuppressWarnings("all")
@Slf4j
public class AlipayServiceImpl implements AlipayService {
    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private AlipayConstant alipayConstant;

    @Autowired
    private PaymentManageService paymentManageService;

    @Autowired
    private RabbitService rabbitService;

    /*
    notify_payment_url: http://n8sa9h.natappfree.cc/api/payment/alipay/callback/notify # 异步通知地址，支付成功后通知商户服务器
    return_order_url: http://payment.gmall.com/pay/success.html # 支付成功后跳转的页面
    return_payment_url: http://api.gmall.com/api/payment/alipay/callback/return # 同步通知地址，支付成功后跳转的页面
    product_code: FAST_INSTANT_TRADE_PAY
     */

    /**
     * 调用 支付宝/微信 支付接口，并返回支付结果
     * @param orderInfo
     * @return
     */
    @Override
    public String pay(OrderInfo orderInfo) {
        try {
            log.info("调用支付宝支付接口，订单号：{}", orderInfo.getOutTradeNo());
            // 构造请求参数以调用接口
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            // 设置订单同步回调和异步通知地址
            request.setReturnUrl(alipayConstant.getReturn_payment_url());
            request.setNotifyUrl(alipayConstant.getNotify_payment_url());
            request.setBizModel(getAlipayTradePagePayModel(orderInfo));

            // 获取支付结果
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request, "POST");
            // 如果需要返回GET请求，请使用
            if (Objects.nonNull(response) && response.isSuccess()) {
                log.info("调用支付宝支付接口成功，订单号：{}", orderInfo.getOutTradeNo());
                // 支付成功，返回支付页面
                return response.getBody();
            }
        } catch (AlipayApiException e) {
            log.error("调用支付宝支付接口失败，订单号：{}, 失败原因：{}", orderInfo.getOutTradeNo(), e.getMessage());
            throw new GmallException("订单支付失败，请稍后重试，谢谢！");
        }
        return "";
    }

    @Override
    public boolean alipayRefund(OrderInfo orderInfo) {
        try {
            log.info("调用支付宝退款接口，订单号：{}",orderInfo.getId());
            PaymentInfo paymentInfo = paymentManageService.selectByOutTradeNoAndPaymentType(orderInfo.getOutTradeNo(), PaymentType.ALIPAY.name());
            if( paymentInfo == null || !PaymentStatus.PAID.name().equals(paymentInfo.getPaymentStatus())) {
                log.error("订单尚未支付，无法退款，订单号：{}", orderInfo.getId());
                return false;
            }
            // 构造请求参数以调用接口
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            request.setBizModel(getAlipayTradeRefundModel(orderInfo));
            AlipayTradeRefundResponse response = alipayClient.execute(request);
            System.out.println(response.getBody());
            if (response.isSuccess()) {
                log.info("调用支付宝退款接口成功，订单号：{}", orderInfo.getId());
                // 退款成功，更新支付状态
                paymentInfo.setPaymentStatus(PaymentStatus.CLOSED.name());
                paymentManageService.updatePaymentInfo(paymentInfo);
                // 更新订单状态
                rabbitService.sendMsg(MqConst.EXCHANGE_DIRECT_PAYMENT_CLOSE, MqConst.ROUTING_PAYMENT_CLOSE, orderInfo.getId());
                return true;
            } else {
                log.error("调用支付宝退款接口失败，订单号：{}, 失败原因：{}", orderInfo.getId(), response.getSubMsg());
                return false;
            }
        } catch (AlipayApiException e) {
            log.error("调用支付宝退款接口失败，订单号：{}, 失败原因：{}", orderInfo.getId(), e.getMessage());
        }
        return false;
    }

    private static AlipayTradeRefundModel getAlipayTradeRefundModel(OrderInfo orderInfo) {
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        /*----------------必填参数------------------*/
        // 设置商户订单号
        model.setOutTradeNo(orderInfo.getOutTradeNo());
        // 设置退款金额
        model.setRefundAmount("0.01");
        /*----------------可选参数------------------*/
        // 设置退款原因说明
        model.setRefundReason("正常退款");
        return model;
    }

    /**
     * 设置支付宝支付参数
     * @param orderInfo
     * @return
     */
    private AlipayTradePagePayModel getAlipayTradePagePayModel(OrderInfo orderInfo) {
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        /*--------------------必填参数------------------*/
        // 设置订单交易号
        model.setOutTradeNo(orderInfo.getOutTradeNo());
        // 设置订单总金额
        model.setTotalAmount("0.01");
        // 设置订单标题
        model.setSubject(orderInfo.getTradeBody());
        // 设置产品码
        model.setProductCode(alipayConstant.getProduct_code());

        /*--------------------可选参数------------------*/
        // 设置订单绝对超时时间
        model.setTimeExpire(getPayExpireTime());
        // 设置PC扫码支付的方式
        // model.setQrPayMode("1");
        // 设置商户自定义二维码宽度
        model.setQrcodeWidth(100L);
        // 设置请求后页面的集成方式
        // model.setIntegrationType("PCWEB");
        return model;
    }

    /**
     * 设置订单超时时间：当前时间 + 10分钟
     */
    private String getPayExpireTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 10);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
    }

    public static void main(String[] args) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 10);
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
    }
}
