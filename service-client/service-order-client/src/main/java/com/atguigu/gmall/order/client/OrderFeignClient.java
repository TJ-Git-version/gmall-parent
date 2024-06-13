package com.atguigu.gmall.order.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.impl.OrderDegradeFeignClient;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@FeignClient(value = "service-order", path = "/api/order", fallback = OrderDegradeFeignClient.class)
public interface OrderFeignClient {

    /**
     * 获取结算订单信息
     */
    @GetMapping("/auth/tradeInfo")
    public Result<Map<String, Object>> getTradeInfo();

    /**
     * 根据订单id查询订单详情
     */
    @GetMapping("/inner/getOrderInfo/{orderId}")
    @ApiOperation("根据订单id查询订单详情")
    public OrderInfo getOrderInfoById(@PathVariable("orderId") Long orderId);
}
