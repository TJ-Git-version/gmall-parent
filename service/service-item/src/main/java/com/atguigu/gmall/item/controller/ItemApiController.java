package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.service.ItemApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 商品详情页接口
 */
@RestController
@RequestMapping("/api/item")
@RequiredArgsConstructor
public class ItemApiController {

    private final ItemApiService itemApiService;


    @GetMapping("/{skuId}")
    public Result<Map<String, Object>> getItemBySkuId(@PathVariable("skuId") Long skuId) {
        Map<String, Object> itemMap = itemApiService.getItemBySkuId(skuId);
        return Result.ok(itemMap);
    }

}
