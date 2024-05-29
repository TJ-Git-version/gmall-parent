package com.atguigu.gmall.list.service.impl;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.GoodsService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.atguigu.gmall.list.service.GoodsService;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("all")
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 商品热度排名添加
     * @param skuId
     */
    @Override
    public void incrHotScore(Long skuId) {
        // 商品热度排名添加
        Double hostScore = redisTemplate.opsForZSet().incrementScore(RedisConst.SKU_HOT_SCORE_KEY, RedisConst.SKU_INFO_KEY_PREFIX + skuId, 1);
        // 判断商品热度排名是否达到了阈值，达到则添加到es中
        if (hostScore % RedisConst.SKU_HOT_SCORE_THRESHOLD == 0) {
            // 查询elasticsearch商品信息
            Goods goods = goodsRepository.findById(skuId).get();
            goods.setHotScore(Math.round(hostScore));
            // 同步商品信息到elasticsearch中
            goodsRepository.save(goods);
        }
    }

    /**
     * 商品下架功能
     *
     * @param skuId
     */
    @Override
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }


    /**
     * 商品上架功能
     * 使用CompletableFuture异步编排优化以下代码
     *
     * @param skuId
     * @return
     */
    @Override
    public Goods upperGoods(Long skuId) {
        // 使用布隆过滤器判断商品是否存在
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        if (!bloomFilter.contains(skuId)) {
            return new Goods();
        }

        // 封装Goods对象
        Goods goods = new Goods();

        // 查询sku商品信息
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            goods.setId(skuInfo.getId());
            goods.setTitle(skuInfo.getSkuName());
            goods.setPrice(skuInfo.getPrice().doubleValue());
            goods.setDefaultImg(skuInfo.getSkuDefaultImg());
            goods.setTmId(skuInfo.getTmId());
            goods.setCreateTime(skuInfo.getCreateTime());
            return skuInfo;
        }, threadPoolExecutor);

        CompletableFuture<Void> attrListCompletableFuture = CompletableFuture.runAsync(() -> {
            // 查询sku的规格参数信息
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
            // 封装sku的规格参数信息
            List<SearchAttr> searchAttrList = attrList.stream().map(arr -> {
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(arr.getId());
                searchAttr.setAttrName(arr.getAttrName());
                BaseAttrValue baseAttrValue = arr.getAttrValueList().stream().findFirst().orElse(new BaseAttrValue());
                searchAttr.setAttrValue(baseAttrValue.getValueName());
                return searchAttr;
            }).collect(Collectors.toList());
            goods.setAttrs(searchAttrList);
        }, threadPoolExecutor);

        // 查询sku的品牌信息
        CompletableFuture<Void> trademarkCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseTrademark skuTrademark = productFeignClient.getTrademark(skuInfo.getTmId());
            // 封装sku品牌信息
            goods.setTmName(skuTrademark.getTmName());
            goods.setTmLogoUrl(skuTrademark.getLogoUrl());
        }, threadPoolExecutor);

        // 查询sku的分类信息
        CompletableFuture<Void> ategoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            // 封装sku分类信息
            BeanUtils.copyProperties(categoryView, goods);
            goods.setId(skuInfo.getId());
        }, threadPoolExecutor);

        CompletableFuture.allOf(
                skuInfoCompletableFuture, trademarkCompletableFuture,
                ategoryViewCompletableFuture, attrListCompletableFuture
        ).join();
        // 同步商品信息到elasticsearch中
        return goodsRepository.save(goods);
    }

}
