package com.atguigu.gmall.product.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.BaseCategoryView;

import java.util.List;

public interface DataViewService {
    /**
     * 根据三级分类id查询一二三级分类视图
     * @param category3Id
     * @return
     */
    BaseCategoryView getBaseCategoryView(Long category3Id);

    /**
     * 查询所有一二三级分类视图
     * @return
     */
    List<JSONObject> getBaseCategoryList();

}
