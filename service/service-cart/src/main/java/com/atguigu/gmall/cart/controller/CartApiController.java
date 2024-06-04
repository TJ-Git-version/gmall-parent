package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartApiService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 购物车API接口
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartApiController {

    private final CartApiService cartApiService;

    ///api/cart/checkCart/21/0
    /**
     * 取消勾选购物车中的商品
     * @param skuId 商品id
     * @param isChecked 0:未勾选 1:勾选
     * @return
     */
    @GetMapping("/checkCart/{skuId}/{isChecked}")
    @ApiOperation("取消勾选购物车中的商品")
    public Result<Void> checkCart(@PathVariable Long skuId, @PathVariable Integer isChecked, HttpServletRequest request) {
        cartApiService.checkCart(skuId, isChecked, request);
        return Result.ok();
    }

    /**
     * 删除购物车中的商品
     * @param skuId
     * @return
     */
     @DeleteMapping("/deleteCart/{skuId}")
     @ApiOperation("删除购物车中的商品")
     public Result<Void> deleteCart(@PathVariable Long skuId, HttpServletRequest request) {
         cartApiService.deleteCart(skuId, request);
         return Result.ok();
     }

    /**
     * 添加商品到购物车
     * @param skuId
     * @param skuNum
     * @return
     */
    @GetMapping("/addToCart/{skuId}/{skuNum}")
    @ApiOperation("添加商品到购物车")
    public Result<Void> addToCart(@PathVariable Long skuId, @PathVariable Integer skuNum, HttpServletRequest request) {
        cartApiService.addCart(skuId, skuNum, request);
        return Result.ok();
    }

    /**
     * 获取购物车列表
     */
    @GetMapping("/cartList")
    @ApiOperation("获取购物车列表")
    public Result<List<CartInfo>> cartList(HttpServletRequest request) {
        return Result.ok(cartApiService.cartList(request));
    }
}
