package com.atguigu.gmall.list.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.impl.ListDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-list", fallback = ListDegradeFeignClient.class)
public interface ListFeignClient {

    /**
     * 商品热度排名增加接口
     *      添加到redis和es中
     */
    @GetMapping("/api/list/inner/incrHotScore/{skuId}")
    public Result<Void> incrHotScore(@PathVariable Long skuId);

}
