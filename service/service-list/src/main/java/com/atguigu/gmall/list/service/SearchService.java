package com.atguigu.gmall.list.service;

import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

public interface SearchService {

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

    /**
     * 商品列表查询
     * @param searchParam
     * @return
     */
    SearchResponseVo searchList(SearchParam searchParam);

}
