package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuImage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface SkuImageMapper extends BaseMapper<SkuImage> {
    /**
     * 批量插入sku图片
     * @param skuImageList
     */
    void insertBatch(List<SkuImage> skuImageList);

}
