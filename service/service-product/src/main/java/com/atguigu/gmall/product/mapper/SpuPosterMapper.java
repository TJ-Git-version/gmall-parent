package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SpuPoster;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface SpuPosterMapper extends BaseMapper<SpuPoster> {

    /**
     * 批量插入海报信息
     * @param spuPosterList
     */
    void insertBatch(List<SpuPoster> spuPosterList);

}
