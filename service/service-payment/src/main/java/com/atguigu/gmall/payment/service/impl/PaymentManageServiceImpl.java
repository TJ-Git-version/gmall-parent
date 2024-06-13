package com.atguigu.gmall.payment.service.impl;

import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentManageService;
import com.google.common.base.Verify;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@SuppressWarnings("all")
@Slf4j
public class PaymentManageServiceImpl implements PaymentManageService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;
    @Autowired
    private OrderFeignClient orderFeignClient;
    @Autowired
    private AlipayService alipayService;

    /**
     * 支付订单
     *
     * @param name
     * @param orderId
     * @param userId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String paymentSubmit(Long orderId, String paymentType, String userId) {
        log.info("订单{}支付方式{}，开始支付", orderId, paymentType);
        OrderInfo orderInfo = orderFeignClient.getOrderInfoById(orderId);
        // 校验订单状态
        verifyOrderStatus(orderId, orderInfo);
        // 先保存订单支付信息
        savePaymentInfo(paymentType, userId, orderInfo);
        // 然后调用支付接口
        String payUrl = alipayService.pay(orderInfo);
        // 最后更新订单状态
        return payUrl;
    }

    /**
     * 保存订单支付信息
     * @param paymentType
     * @param userId
     * @param orderInfo
     * @return
     */
    public boolean savePaymentInfo(String paymentType, String userId, OrderInfo orderInfo) {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setUserId(Long.valueOf(userId));
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setTotalAmount(new BigDecimal(0.01));
        paymentInfo.setSubject(orderInfo.getTradeBody());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.name());
        return paymentInfoMapper.insert(paymentInfo) > 0;
    }

    private static void verifyOrderStatus(Long orderId, OrderInfo orderInfo) {
        if (Objects.isNull(orderInfo)) {
            log.error("订单{}不存在!", orderId);
            throw new GmallException("当前订单不存在!");
        }
        if (Objects.equals(orderInfo.getOrderStatus(), OrderStatus.PAID.name()) ||
                Objects.equals(orderInfo.getOrderStatus(), OrderStatus.CLOSED.name())) {
            log.error("该订单已经完成或已经关闭!");
            throw new GmallException("该订单已经完成或已经关闭!");
        }
    }
}
