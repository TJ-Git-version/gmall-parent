package com.atguigu.gmall.order.controller;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderManagerService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderManagerService orderManagerService;

    /**
     * 根据用户id获取订单列表
     */
    @GetMapping("auth/{current}/{limit}")

    @ApiOperation("根据用户id获取订单列表")
    public Result<IPage<OrderInfo>> getMyOrderByUserId(@PathVariable("current") Integer current, @PathVariable("limit") Integer limit, HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        Page<OrderInfo> page = new Page<>(current, limit);
        return Result.ok(orderManagerService.getMyOrderByUserId(page, userId));
    }

    /**
     * 提交订单
     */
    @PostMapping("/auth/submitOrder")
    @ApiOperation("提交订单")
    public Result<Object> submitOrder(@RequestBody OrderInfo orderInfo, @RequestParam("tradeNo") String tradeNo, HttpServletRequest request) {
        // 校验订单流水号
        Boolean checkTradeNo = orderManagerService.checkTradeNo(tradeNo);
        if (!checkTradeNo) {
            return Result.fail().message("当前订单已提交，请勿重复提交");
        }
        String userId = AuthContextHolder.getUserId(request);
        // 校验商品库存是否不足和价格是否正确
        List<String> errorSkuList = orderManagerService.checkSkuStockAndPrice(orderInfo.getOrderDetailList());
        if (CollectionUtils.isNotEmpty(errorSkuList)) {
            // 重新获取商品信息，更新勾选购物车的缓存数据
            orderManagerService.updateCartCache(userId);
            return Result.fail().message(errorSkuList.get(0));
        }
        orderInfo.setUserId(Long.valueOf(userId));
        Long orderId = orderManagerService.submitOrder(orderInfo, tradeNo);
        return Result.ok(orderId);
    }

    /**
     * 获取结算订单信息
     */
    @GetMapping("/auth/tradeInfo")
    @ApiOperation("获取结算订单信息")
    public Result<Map<String, Object>> getTradeInfo(HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        Map<String, Object> resultMap = orderManagerService.getTradeInfoByUserId(userId);
        return Result.ok(resultMap);
    }

}
