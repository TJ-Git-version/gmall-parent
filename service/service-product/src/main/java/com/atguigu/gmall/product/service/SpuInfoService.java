package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SpuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface SpuInfoService {


    /**
     * 根据三级分类id获取spu信息列表
     * @param page
     * @param category3Id
     * @return
     */
    IPage<SpuInfo> getSpuInfosPage(Page<SpuInfo> page, Long category3Id);

}
