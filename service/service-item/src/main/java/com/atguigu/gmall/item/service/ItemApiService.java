package com.atguigu.gmall.item.service;

import java.util.Map;

public interface ItemApiService {
    /**
     * 根据skuId查询商品详情
     * @param skuId
     * @return
     */
    Map<String, Object> getItemBySkuId(Long skuId);
}
