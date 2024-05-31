package com.atguigu.gmall.list.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ListDegradeFeignClient implements ListFeignClient {
    @Override
    public Result<Void> incrHotScore(Long skuId) {
        return null;
    }

    @Override
    public Result<Map<String, Object>> searchList(SearchParam searchParam) {
        return Result.fail();
    }
}
