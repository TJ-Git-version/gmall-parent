package com.atguigu.gmall.weball.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartFeignClient cartFeignClient;

    private final ProductFeignClient productFeignClient;


    /**
     * 我的购物车列表
     */
    @GetMapping("/cart.html")
    public String cartList() {
        return "cart/index";
    }

    // addCart.html?skuId=22&skuNum=1&sourceType=query

    /**
     * 添加购物车调整成功页面
     */
    @GetMapping("/addCart.html")
    public String addCart(Long skuId, Integer skuNum, String sourceType, Model model) {
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        model.addAttribute("skuInfo", skuInfo);
        model.addAttribute("skuNum", skuNum);
        model.addAttribute("sourceType", sourceType);
        return "cart/addCart";
    }

}
