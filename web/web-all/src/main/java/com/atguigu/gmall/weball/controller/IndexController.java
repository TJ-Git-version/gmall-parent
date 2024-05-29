package com.atguigu.gmall.weball.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.product.client.ProductFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final ProductFeignClient productFeignClient;

    @GetMapping({"/","index.html"})
    public String index(Model model) {
        List<JSONObject> baseCategoryList = productFeignClient.getBaseCategoryList();
        model.addAttribute("list", baseCategoryList);
        return "index/index";
    }

}
