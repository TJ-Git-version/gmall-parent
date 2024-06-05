package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderManagerService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderManagerService orderManagerService;

    /**
     * 提交订单
     */
    @PostMapping("/auth/submitOrder")
    @ApiOperation("提交订单")
    public Result<Object> submitOrder(@RequestBody OrderInfo orderInfo,@RequestParam("tradeNo") String tradeNo, HttpServletRequest request) {
        // 校验订单流水号
        Boolean checkTradeNo = orderManagerService.checkTradeNo(tradeNo);
        if (!checkTradeNo) {
            return Result.fail().message("当前订单已提交，请勿重复提交");
        }
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.valueOf(userId));
        Long orderId = orderManagerService.submitOrder(orderInfo);
        // 提交订单成功后，删除订单流水号
        orderManagerService.deleteTradeNo(tradeNo);
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
