package com.atguigu.gmall.payment.service;


import com.atguigu.gmall.model.order.OrderInfo;

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
     * @param paymentType
     * @param userId
     * @param orderInfo
     * @return
     */
    public boolean savePaymentInfo(String paymentType, String userId, OrderInfo orderInfo);
}
