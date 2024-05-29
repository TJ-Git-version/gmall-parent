package com.atguigu.gmall.list.service;

import com.atguigu.gmall.model.list.Goods;

public interface GoodsService {

    /**
     * 商品上架功能
     * @param skuId
     * @return
     */
    Goods upperGoods(Long skuId);

    /**
     * 商品下架功能
     * @param skuId
     */
    void lowerGoods(Long skuId);

    /**
     * 商品热度增加功能
     * @param skuId
     */
    void incrHotScore(Long skuId);
}
