package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.service.GoodsService;
import com.atguigu.gmall.model.list.Goods;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/list")
@RequiredArgsConstructor
@Slf4j
public class ListApiController {

    private final ElasticsearchRestTemplate esRestTemplate;

    private final GoodsService goodsService;

    /**
     * 商品热度排名增加接口
     *      添加到redis和es中
     */
    @GetMapping("/inner/incrHotScore/{skuId}")
    @ApiOperation("商品热度排名增加api")
    public Result<Void> incrHotScore(@PathVariable Long skuId) {
        goodsService.incrHotScore(skuId);
        return Result.ok();
    }
    /**
     * 商品下架功能
     */
    @GetMapping("/inner/lowerGoods/{skuId}")
    @ApiOperation("商品下架api")
    public Result<Void> lowerGoods(@PathVariable Long skuId) {
        goodsService.lowerGoods(skuId);
        return Result.ok();
    }

    /**
     * 商品上架功能
     */
    @GetMapping("/inner/upperGoods/{skuId}")
    @ApiOperation("商品上架api")
    public Result<Goods> upperGoods(@PathVariable Long skuId) {
        return Result.ok(goodsService.upperGoods(skuId));
    }

    /**
     * 初始化mapping结构到es中
     * @return
     */
    @GetMapping("/createIndex")
    public Result<Void> createIndex() {
        try {
            // 创建索引
            boolean flag = esRestTemplate.indexOps(Goods.class).create();
            // 创建映射
            // Document document = esRestTemplate.indexOps(Goods.class).createMapping();
            boolean document = esRestTemplate.putMapping(Goods.class);
            log.info("goods 创建索引成功：{}", flag);
            log.info("goods 创建映射成功：{}", document);
        } catch (Exception e) {
            log.error("创建索引失败：{}", e.getMessage());
            throw new GmallException("该索引已存在，请勿重复创建");
        }
        return Result.ok();
    }

    @GetMapping("/deleteIndex")
    public Result<Void> deleteIndex(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            // boolean flag = esRestTemplate.indexOps(clazz).delete();
            boolean flag = esRestTemplate.deleteIndex(clazz);
            log.info("goods 删除索引成功：{}",flag);
        } catch (Exception e) {
            log.error("删除索引失败：{}", e.getMessage());
            throw new GmallException("删除索引失败，请检查className是否正确");
        }
        return Result.ok();
    }

    public static void main(String[] args) throws ClassNotFoundException {
    }
}
