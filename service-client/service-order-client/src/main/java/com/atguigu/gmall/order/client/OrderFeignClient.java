package com.atguigu.gmall.order.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.order.client.impl.OrderDegradeFeignClient;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@FeignClient(value = "service-order", path = "/api/order", fallback = OrderDegradeFeignClient.class)
public interface OrderFeignClient {

    /**
     * 获取结算订单信息
     */
    @GetMapping("/auth/tradeInfo")
    public Result<Map<String, Object>> getTradeInfo();

}
