package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {
    /**
     * 批量插入销售属性值
     * @param skuSaleAttrValueList
     */
    void insertBatch(List<SkuSaleAttrValue> skuSaleAttrValueList);

    /**
     * 根据spuId 查询sku的销售属性值，使用Map封装返回数据
     * @param spuId
     * @return
     */
    List<Map<Object, Object>> getSkuValueIdsMap(Long spuId);
}
