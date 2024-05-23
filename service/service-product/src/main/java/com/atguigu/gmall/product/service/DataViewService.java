package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseCategoryView;

public interface DataViewService {
    /**
     * 根据三级分类id查询一二三级分类视图
     * @param category3Id
     * @return
     */
    BaseCategoryView getBaseCategoryView(Long category3Id);
}
