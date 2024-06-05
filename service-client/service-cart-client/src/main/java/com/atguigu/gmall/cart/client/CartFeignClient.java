package com.atguigu.gmall.cart.client;

import com.atguigu.gmall.cart.client.impl.CartDegradeFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@FeignClient(value = "service-cart", path = "/api/cart", fallback = CartDegradeFeignClient.class)
public interface CartFeignClient {

    /**
     * 获取当前用户勾选购物车列表
     * @return
     */
    @GetMapping("/inner/getCartCheckedList/{userId}")
    @ApiOperation("获取当前用户勾选购物车列表")
    public List<CartInfo> getCartCheckedList(@PathVariable String userId);

}
