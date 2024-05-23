package com.atguigu.gmall.item.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ItemDegradeFeignClient implements ItemFeignClient {
    @Override
    public Result<Map<String, Object>> getItemBySkuId(Long skuId) {
        return Result.fail();
    }
}
