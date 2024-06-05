package com.atguigu.gmall.weball.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.order.client.OrderFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderFeignClient orderFeignClient;

    @GetMapping("/trade.html")
    public String trade(Model model){
        Result<Map<String, Object>> tradeInfo = orderFeignClient.getTradeInfo();
        if (tradeInfo.getCode() == 200) {
            model.addAllAttributes(tradeInfo.getData());
        }
        return "order/trade";
    }

}
