package com.atguigu.gmall.weball.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemFeignClient itemFeignClient;

    @GetMapping("/{skuId}.html")
    public String getProductItem(@PathVariable Long skuId, Model model) {
        Result<Map<String, Object>> result = itemFeignClient.getItemBySkuId(skuId);
        model.addAllAttributes(result.getData());
        return "item/item";
    }

}
