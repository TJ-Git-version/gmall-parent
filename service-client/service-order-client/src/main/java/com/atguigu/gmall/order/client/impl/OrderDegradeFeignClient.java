package com.atguigu.gmall.order.client.impl;

import com.atguigu.gmall.common.result.Result;
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
}
