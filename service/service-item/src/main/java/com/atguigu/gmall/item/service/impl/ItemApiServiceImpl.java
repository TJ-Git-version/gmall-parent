package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.item.service.ItemApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ItemApiServiceImpl implements ItemApiService {

    // @Autowired
    // private ProductFeignClient productFeignClient;

    /**
     * 根据skuId查询商品详情
     * @param skuId
     * @return
     */
    @Override
    public Map<String, Object> getItemBySkuId(Long skuId) {
        Map<String, Object> itemMap = new HashMap<>();
        return itemMap;
    }
}
