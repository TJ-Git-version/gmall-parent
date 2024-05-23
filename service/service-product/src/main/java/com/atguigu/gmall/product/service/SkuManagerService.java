package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface SkuManagerService {


    /**
     * 根据skuId查询sku价格
     * @param skuId
     * @return
     */
    BigDecimal getSkuPrice(Long skuId);

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

    /**
     * 根据skuId查询sku信息
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfo(Long skuId);

    /**
     * 根据skuId和spuId查询商品销售属性组合
     * @param skuId
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId);

    /**
     * 通过skuId获取sku对应的平台属性
     * @param skuId
     * @return
     */
    List<BaseAttrInfo> getAttrList(Long skuId);

    /**
     * 根据spuId 查询sku的销售属性值，使用Map封装返回数据
     * @param spuId
     * @return
     */
    Map<Object, Object> getSkuValueIdsMap(Long spuId);
}
