package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface CartApiService {

    /**
     * 添加购物车
     * @param skuId
     * @param skuNum
     * @param request
     */
    void addCart(Long skuId, Integer skuNum, HttpServletRequest request);

    /**
     * 获取购物车列表
     * @param request
     * @return
     */
    List<CartInfo> cartList(HttpServletRequest request);

    /**
     * 删除购物车商品
     * @param skuId
     * @param request
     */
    void deleteCart(Long skuId, HttpServletRequest request);

    /**
     * 取消勾选购物车中的商品
     * @param skuId
     * @param isChecked
     * @param request
     */
    void checkCart(Long skuId, Integer isChecked, HttpServletRequest request);

    /**
     * 获取当前用户勾选的购物车列表
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);

}
