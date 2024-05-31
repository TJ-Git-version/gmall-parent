package com.atguigu.gmall.list.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.impl.ListDegradeFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(value = "service-list", path = "/api/list", fallback = ListDegradeFeignClient.class)
public interface ListFeignClient {

    /**
     * 商品热度排名增加接口
     *      添加到redis和es中
     */
    @GetMapping("/inner/incrHotScore/{skuId}")
    public Result<Void> incrHotScore(@PathVariable Long skuId);

    /**
     * 全文检索-商品搜索接口
     * @param searchParam
     * @return
     */
    @PostMapping
    public Result<Map<String, Object>> searchList(@RequestBody SearchParam searchParam);


}
