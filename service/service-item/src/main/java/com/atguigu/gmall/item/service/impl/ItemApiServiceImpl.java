package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.item.service.ItemApiService;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("all")
public class ItemApiServiceImpl implements ItemApiService {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private ListFeignClient listFeignClient;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ThreadPoolExecutor executor;

    /**
     * 根据skuId查询商品详情
     *
     * @param skuId
     * @return
     */
    @Override
    public Map<String, Object> getItemBySkuId(Long skuId) {
        Map<String, Object> itemMap = new HashMap<>();

        // 通过布隆过滤器判断是否存在该商品，若不存在直接返回空对象
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        if (!bloomFilter.contains(skuId)) {
            return itemMap;
        }

        // 查询sku信息和sku图片信息 skuInfo
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            itemMap.put("skuInfo", skuInfo);
            return skuInfo;
        }, executor);

        // 查询实时价格信息 price
        CompletableFuture<Void> skuPriceCompletableFuture = CompletableFuture.runAsync(() -> {
            BigDecimal price = productFeignClient.getSkuPrice(skuId);
            itemMap.put("price", price);
        }, executor);

        // 查询sku平台属性信息 skuAttrList
        CompletableFuture<Void> skuAttrListCompletableFuture = CompletableFuture.runAsync(() -> {
            List<BaseAttrInfo> skuAttrList = productFeignClient.getAttrList(skuId);
            List<Map<String, Object>> skuAttrMapList = skuAttrList.stream().map(skuAttr -> {
                Map<String, Object> map = new HashMap<>();
                map.put("attrName", skuAttr.getAttrName());
                map.put("attrValue", skuAttr.getAttrValueList().stream().findFirst().orElse(new BaseAttrValue()).getValueName());
                return map;
            }).collect(Collectors.toList());
            itemMap.put("skuAttrList", skuAttrMapList);
        }, executor);

        // 查询spu属性信息，并根据sku标记出当前sku的规格属性 spuSaleAttrList
        CompletableFuture<Void> spuSaleAttrListCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
            itemMap.put("spuSaleAttrList", spuSaleAttrList);
        }, executor);

        // 查询三级分类信息 categoryView
        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            itemMap.put("categoryView", categoryView);
        }, executor);

        // 查询商品切换需要的数据 valuesSkuJson
        CompletableFuture<Void> skuValueIdsMapCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            Map<Object, Object> valuesSkuJson = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            itemMap.put("valuesSkuJson", JSON.toJSONString(valuesSkuJson));
        }, executor);

        // 获取spu海报信息 spuPosterList
        CompletableFuture<Void> spuPosterListCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            List<SpuPoster> spuPosterList = productFeignClient.findSpuPosterBySpuId(skuInfo.getSpuId());
            itemMap.put("spuPosterList", spuPosterList);
        }, executor);

        // 更新商品热度排名记录
        CompletableFuture<Void> hotScoreCompletableFuture = CompletableFuture.runAsync(() -> {
            listFeignClient.incrHotScore(skuId);
        });

        // 等待所有任务完成
        CompletableFuture.allOf(
                skuInfoCompletableFuture, skuPriceCompletableFuture,
                skuAttrListCompletableFuture, spuSaleAttrListCompletableFuture,
                categoryViewCompletableFuture, skuValueIdsMapCompletableFuture,
                spuPosterListCompletableFuture,hotScoreCompletableFuture
        ).join();
        return itemMap;
    }
}
