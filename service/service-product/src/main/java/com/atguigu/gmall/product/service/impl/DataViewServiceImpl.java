package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.product.mapper.BaseCategoryViewMapper;
import com.atguigu.gmall.product.service.DataViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("all")
public class DataViewServiceImpl implements DataViewService {

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    /**
     * 根据三级分类id查询一二三级分类
     * @param category3Id
     * @return
     */
    @Override
    public BaseCategoryView getBaseCategoryView(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }
}
