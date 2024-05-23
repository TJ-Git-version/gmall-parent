package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.SkuAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;

import java.util.List;
import java.util.Map;

public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValue> {
    /**
     * 批量插入sku属性值
     * @param skuAttrValueList
     */
    void insertBatch(List<SkuAttrValue> skuAttrValueList);
}
