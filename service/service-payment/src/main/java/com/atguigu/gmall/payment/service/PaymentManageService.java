package com.atguigu.gmall.payment.service;


import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;

import java.util.Map;

public interface PaymentManageService {

    /**
     * 支付订单
     *
     * @param orderId
     * @param paymentType
     * @param userId
     * @return
     */
    String paymentSubmit(Long orderId, String paymentType, String userId);

    /**
     * 保存支付信息
     *
     * @param paymentType
     * @param userId
     * @param orderInfo
     * @return
     */
    public boolean savePaymentInfo(String paymentType, String userId, OrderInfo orderInfo);

    /**
     * 支付宝同步回调，跳转到支付成功页面
     *
     * @return
     */
    String alipayCallback();

    /**
     * 支付宝异步回调，更新订单状态
     *
     * @param paramsMap
     * @return
     */
    String alipayCallbackNotify(Map<String, String> paramsMap);

    /**
     * 根据订单交易号和支付方式查询支付信息
     *
     * @param outTradeNo
     * @param paymentType
     * @return
     */
    PaymentInfo selectByOutTradeNoAndPaymentType(String outTradeNo, String paymentType);

    /**
     * 更新支付状态
     *
     * @param paramsMap
     * @param paymentInfo
     */
    void updatePaymentStatus(Map<String, String> paramsMap, PaymentInfo paymentInfo, String paymentStatus);

    /**
     * 修改支付信息和订单信息状态
     * @param paramsMap
     * @param paymentInfo
     * @param paymentStatus
     */
    void updatePaymentAndOrderStatus(Map<String, String> paramsMap, PaymentInfo paymentInfo, String paymentStatus);


    /**
     * 支付宝退款
     *
     * @param orderId
     * @return
     */
    boolean alipayRefund(Long orderId);

    /**
     * 根据订单号查询支付信息
     * @param orderId
     * @return
     */
    PaymentInfo getPaymentInfoByOrderId(Long orderId);

    /**
     * 更新支付信息
     * @param paymentInfo
     */
    void updatePaymentInfo(PaymentInfo paymentInfo);
}
