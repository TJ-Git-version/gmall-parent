package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BaseCategoryTrademarkService extends IService<BaseCategoryTrademark> {

    /**
     * 根据三级分类id查询分类品牌列表
     * @param category3Id
     * @return
     */
    List<BaseTrademark> findTrademarkList(Long category3Id);

    /**
     * 根据三级分类id查询当前品牌列表
     * @param category3Id
     * @return
     */
    List<BaseTrademark> findCurrentTrademarkList(Long category3Id);

    /**
     * 新增分类品牌关系
     * @param categoryTrademarkVo
     */
    void saveCategoryTrademark(CategoryTrademarkVo categoryTrademarkVo);
}
