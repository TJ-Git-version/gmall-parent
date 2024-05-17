package com.atguigu.gmall.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SaleStatus {

    CANCEL_SALE(0, "已下架"),
    ON_SALE(1, "在售");


    private final int status;
    private final String desc;
}
