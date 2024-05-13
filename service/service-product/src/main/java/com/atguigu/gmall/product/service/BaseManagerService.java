package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;

import java.util.List;

public interface BaseManagerService {

    /**
     * 获取一级分类列表
     * @return
     */
    List<BaseCategory1> getCategory1List();

    /**
     * 获取二级分类列表
     * @param category1Id
     * @return
     */
    List<BaseCategory2> getCategory2List(Long category1Id);

    /**
     * 获取三级分类列表
     * @param category2Id
     * @return
     */
    List<BaseCategory3> getCategory3List(Long category2Id);

    /**
     * 根据分类id获取属性列表
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id);
}
