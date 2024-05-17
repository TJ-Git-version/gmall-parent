package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {
    /**
     * 批量插入销售属性值
     * @param skuSaleAttrValueList
     */
    void insertBatch(List<SkuSaleAttrValue> skuSaleAttrValueList);

}
