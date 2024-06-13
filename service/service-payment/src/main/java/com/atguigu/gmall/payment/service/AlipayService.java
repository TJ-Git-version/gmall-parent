package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.order.OrderInfo;

public interface AlipayService {


    String pay(OrderInfo orderInfo);

}
