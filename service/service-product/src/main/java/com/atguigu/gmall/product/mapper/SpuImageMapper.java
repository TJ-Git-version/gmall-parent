package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SpuImage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface SpuImageMapper extends BaseMapper<SpuImage> {

    /**
     * 批量插入spu图片信息
     * @param spuImageList
     */
    void insertBatch(List<SpuImage> spuImageList);

}
