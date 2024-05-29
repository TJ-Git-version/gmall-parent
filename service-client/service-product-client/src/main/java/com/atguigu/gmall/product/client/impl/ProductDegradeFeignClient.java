package com.atguigu.gmall.product.client.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class ProductDegradeFeignClient implements ProductFeignClient {
    @Override
    public List<JSONObject> getBaseCategoryList() {
        return null;
    }

    @Override
    public Map<Object, Object> getSkuValueIdsMap(Long spuId) {
        return null;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        return null;
    }

    @Override
    public List<SpuImage> findSpuPosterBySpuId(Long spuId) {
        return null;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return null;
    }

    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        return null;
    }

    @Override
    public BaseCategoryView getCategoryView(Long category3Id) {
        return null;
    }

    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        return null;
    }
}
