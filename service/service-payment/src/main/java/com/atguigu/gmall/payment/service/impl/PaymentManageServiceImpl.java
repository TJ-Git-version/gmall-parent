package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.payment.constant.AlipayConstant;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentManageService;
import com.atguigu.gmall.rabbit.constant.MqConst;
import com.atguigu.gmall.rabbit.service.RabbitService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    private AlipayConstant alipayConstant;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RabbitService rabbitService;

    /**
     * 支付宝退款
     *
     * @param orderId
     * @param paramsMap
     * @return
     */
    @Override
    public boolean alipayRefund(Long orderId) {
        return alipayService.alipayRefund(orderFeignClient.getOrderInfoById(orderId));
    }

    /**
     * 根据订单号查询支付信息
     *
     * @param orderId
     * @return
     */
    @Override
    public PaymentInfo getPaymentInfoByOrderId(Long orderId) {
        return paymentInfoMapper.selectOne(
                Wrappers.<PaymentInfo>lambdaQuery()
                        .eq(PaymentInfo::getOrderId, orderId)
        );
    }

    @Override
    public void updatePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.updateById(paymentInfo);
    }


    /**
     * 支付宝支付同步回调
     */
    @Override
    public String alipayCallback() {
        return "redirect:" + alipayConstant.getReturn_order_url();
    }

    /**
     * 支付宝支付异步回调
     * publicKey：支付宝公钥，用于验签，给支付宝服务器发送请求时使用
     * charset： 字符编码，固定值：utf-8
     * signType： 签名类型，固定值：RSA2
     *
     * @param paramsMap
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String alipayCallbackNotify(Map<String, String> paramsMap) {
        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(paramsMap, alipayConstant.getPublic_key(), alipayConstant.getCharset(), alipayConstant.getSign_type()); // 调用SDK验证签名
            // 验签成功, 开始处理业务逻辑
            if (signVerified) {
                /*
                商家需要验证该通知数据中的 out_trade_no 是否为商家系统中创建的订单号。
                判断 total_amount 是否确实为该订单的实际金额（即商家订单创建时的金额）。
                校验通知中的 seller_id（或者 seller_email）是否为 out_trade_no 这笔单据的对应的操作方（有的时候，一个商家可能有多个 seller_id/seller_email）。
                验证 app_id 是否为该商家本身
                 */
                String out_trade_no = paramsMap.get("out_trade_no");
                String total_amount = paramsMap.get("total_amount");
                String seller_id = paramsMap.get("seller_id");
                String app_id = paramsMap.get("app_id");
                String tradeStatus = paramsMap.get("trade_status");
                // 使用notify_id来解决重复通知产生支付状态和订单状态重复修改
                String notifyId = paramsMap.get("notify_id");
                Boolean flag = redisTemplate.opsForValue().setIfAbsent(notifyId, notifyId, 900, TimeUnit.MINUTES);
                if (flag) {
                    log.info("支付宝支付异步回调，订单号：{}，支付金额：{}，商家ID：{}，应用ID：{}", out_trade_no, total_amount, seller_id, app_id);
                    PaymentInfo paymentInfo = selectByOutTradeNoAndPaymentType(out_trade_no, PaymentType.ALIPAY.name());
                    // 根据交易订单号和支付类型获取支付之前保存的支付信息，并校验支付宝返回的金额是否与订单金额一致
                    if (verifyPaymentInformation(out_trade_no, total_amount, app_id, tradeStatus, paymentInfo))
                        return "failure";
                    if (StringUtils.equals(tradeStatus, "TRADE_SUCCESS") || StringUtils.equals(tradeStatus, "TRADE_FINISHED")) {
                        // 更新支付状态
                        updatePaymentAndOrderStatus(paramsMap, paymentInfo, PaymentStatus.PAID.name());
                        // orderFeignClient.updateOrderStatus(paymentInfo.getOrderId(), ProcessStatus.PAID); 网络延迟，导致订单状态更新失败，不推荐使用

                        return "success";
                    }
                } else {
                    return "success";
                }
            } else {
                // 验签失败则记录异常日志，并在response中返回failure.
                return "failure";
            }
        } catch (AlipayApiException e) {
            redisTemplate.delete(paramsMap.get("notify_id"));
            log.error("支付宝支付异步回调异常：{}", e.getMessage());
            throw new GmallException("支付宝支付异步回调异常：" + e.getMessage());
        }
        return "failure";
    }

    @Override
    public void updatePaymentAndOrderStatus(Map<String, String> paramsMap, PaymentInfo paymentInfo, String paymentStatus) {
        // 修改支付信息状态
        updatePaymentStatus(paramsMap, paymentInfo, paymentStatus);
        // 修改订单状态
        // 更新订单状态，使用mq通知订单服务更新订单状态
        rabbitService.sendMsg(MqConst.EXCHANGE_DIRECT_PAYMENT_PAY, MqConst.ROUTING_PAYMENT_PAY, paymentInfo.getOrderId());
    }

    private boolean verifyPaymentInformation(String out_trade_no, String total_amount, String app_id, String tradeStatus, PaymentInfo paymentInfo) {
        return Objects.isNull(paymentInfo) &&
                !StringUtils.equals(paymentInfo.getOutTradeNo(), out_trade_no) &&
                !StringUtils.equals(paymentInfo.getPaymentStatus(), PaymentStatus.UNPAID.name()) &&
                !StringUtils.equals(String.valueOf(paymentInfo.getTotalAmount()), total_amount) &&
                !StringUtils.equals(app_id, alipayConstant.getApp_id());
    }

    /**
     * 更新支付状态
     *
     * @param paramsMap
     * @param paymentInfo
     */
    public void updatePaymentStatus(Map<String, String> paramsMap, PaymentInfo paymentInfo, String paymentStatus) {
        try {
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setTradeNo(paramsMap.get("trade_no"));
            paymentInfo.setCallbackContent(JSON.toJSONString(paramsMap));
            paymentInfo.setPaymentStatus(paymentStatus);
            paymentInfoMapper.updateById(paymentInfo);
        } catch (Exception e) {
            // 出现异常释放锁
            redisTemplate.delete(paramsMap.get("notify_id"));
            log.info("更新支付状态异常，请联系管理员");
        }
    }


    /**
     * 根据订单号和支付类型查询支付信息
     *
     * @param outTradeNo
     * @param paymentType
     * @return
     */
    public PaymentInfo selectByOutTradeNoAndPaymentType(String outTradeNo, String paymentType) {
        return paymentInfoMapper.selectOne(Wrappers.<PaymentInfo>lambdaQuery()
                .eq(PaymentInfo::getOutTradeNo, outTradeNo)
                .eq(PaymentInfo::getPaymentType, paymentType)
        );
    }

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
     *
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

    /**
     * 校验订单状态
     *
     * @param orderId
     * @param orderInfo
     */
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
