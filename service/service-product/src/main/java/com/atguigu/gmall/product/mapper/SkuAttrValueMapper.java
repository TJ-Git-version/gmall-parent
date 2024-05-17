package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValue> {
    /**
     * 批量插入sku属性值
     * @param skuAttrValueList
     */
    void insertBatch(List<SkuAttrValue> skuAttrValueList);
}
