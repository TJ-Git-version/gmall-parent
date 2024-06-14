package com.atguigu.gmall.order.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
public class OrderDegradeFeignClient implements OrderFeignClient {
    @Override
    public Result<Map<String, Object>> getTradeInfo() {
        return Result.fail();
    }

    @Override
    public OrderInfo getOrderInfoById(Long orderId) {
        return new OrderInfo();
    }

    @Override
    public Result<Void> updateOrderStatus(Long orderId, ProcessStatus processStatus) {
        return Result.fail();
    }
}
