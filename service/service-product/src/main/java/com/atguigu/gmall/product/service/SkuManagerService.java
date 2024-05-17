package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface SkuManagerService {

    /**
     * 保存sku信息
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /**
     * 分页查询sku信息
     * @param page
     * @return
     */
    IPage<SkuInfo> getSkuListPage(Page<SkuInfo> page);

    /**
     * 上架/下架sku
     *
     * @param skuId
     * @param isSale 是否销售（1：是 0：否）
     */
    void skuSaleStatusChange(Long skuId, Integer isSale);
}
