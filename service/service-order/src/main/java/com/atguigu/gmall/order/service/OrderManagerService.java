package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;

import java.util.Map;

public interface OrderManagerService {

    /**
     * 根据用户id查询交易信息
     * @param userId
     * @return
     */
    Map<String, Object> getTradeInfoByUserId(String userId);

    /**
     * 提交订单
     * @param orderInfo
     * @return
     */
    Long submitOrder(OrderInfo orderInfo);

    /**
     * 校验订单流水号
     * @param tradeNo
     * @return
     */
    Boolean checkTradeNo(String tradeNo);

    /**
     * 根据交易号删除订单流水号
     * @param tradeNo
     */
    void deleteTradeNo(String tradeNo);
}
