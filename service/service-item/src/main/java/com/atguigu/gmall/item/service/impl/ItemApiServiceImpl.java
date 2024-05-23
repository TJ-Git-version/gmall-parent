package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemApiService;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("all")
public class ItemApiServiceImpl implements ItemApiService {

    @Autowired
    private ProductFeignClient productFeignClient;

    /**
     * 根据skuId查询商品详情
     * @param skuId
     * @return
     */
    @Override
    public Map<String, Object> getItemBySkuId(Long skuId) {
        Map<String, Object> itemMap = new HashMap<>();
        // 1. 查询sku信息和sku图片信息 skuInfo
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        if (ObjectUtils.isNotNull(skuInfo)) {
            itemMap.put("skuInfo", skuInfo);
            // 2. 查询spu属性信息，并根据sku标记出当前sku的规格属性 spuSaleAttrList
            List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
            itemMap.put("spuSaleAttrList", spuSaleAttrList);

            // 3. 查询三级分类信息 categoryView
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            itemMap.put("categoryView", categoryView);

            // 4. 查询实时价格信息 price
            BigDecimal price = productFeignClient.getSkuPrice(skuId);
            itemMap.put("price", price);

            // 5. 查询sku平台属性信息 skuAttrList

            List<BaseAttrInfo> skuAttrList = productFeignClient.getAttrList(skuId);
            List<Map<String, Object>> skuAttrMapList = skuAttrList.stream().map(skuAttr -> {
                Map<String, Object> map = new HashMap<>();
                map.put("attrName", skuAttr.getAttrName());
                map.put("attrValue", skuAttr.getAttrValueList().stream().findFirst().orElse(new BaseAttrValue()).getValueName());
                return map;
            }).collect(Collectors.toList());
            itemMap.put("skuAttrList", skuAttrMapList);

            // 6. 查询商品切换需要的数据 valuesSkuJson
            Map<Object, Object> valuesSkuJson = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            itemMap.put("valuesSkuJson", JSON.toJSONString(valuesSkuJson));

            // 7. 获取spu海报信息 spuPosterList
            List<SpuImage> spuPosterList = productFeignClient.findSpuPosterBySpuId(skuInfo.getSpuId());
            itemMap.put("spuPosterList", spuPosterList);
        }
        return itemMap;
    }
}
