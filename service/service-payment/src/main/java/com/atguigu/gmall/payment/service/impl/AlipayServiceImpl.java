package com.atguigu.gmall.payment.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.*;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.payment.constant.AlipayConstant;
import com.atguigu.gmall.payment.service.AlipayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

@Service
@SuppressWarnings("all")
@Slf4j
public class AlipayServiceImpl implements AlipayService {
    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private AlipayConstant alipayConstant;


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
            request.setReturnUrl(alipayConstant.getReturn_order_url());
            // request.setNotifyUrl(alipayConstant.getNotify_payment_url());
            request.setBizModel(getAlipayTradePagePayModel(orderInfo));

            // 获取支付结果
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request, "POST");
            // 如果需要返回GET请求，请使用
            if (response.isSuccess()) {
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
        model.setQrPayMode("1");
        // 设置商户自定义二维码宽度
        model.setQrcodeWidth(100L);
        // 设置请求后页面的集成方式
        model.setIntegrationType("PCWEB");
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
