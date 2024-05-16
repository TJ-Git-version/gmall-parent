package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface BaseCategoryTrademarkMapper extends BaseMapper<BaseCategoryTrademark> {

    /**
     * 根据三级分类id查询品牌分类列表
     * @param category3Id
     * @return
     */
    List<BaseTrademark> findTrademarkList(Long category3Id);

}
