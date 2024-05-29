package com.atguigu.gmall.product.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.*;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product/inner")
@RequiredArgsConstructor
public class ProductApiController {

    private final SkuManagerService skuManagerService;

    private final SpuManagerService spuManagerService;

    private final DataViewService dataViewService;

    private final BaseTrademarkService trademarkService;

    /**
     * 根据品牌id查询品牌信息
     * @param tmId
     * @return
     */
    @GetMapping("/getTrademark/{tmId}")
    @ApiOperation("根据品牌id查询品牌信息")
    public BaseTrademark getTrademark(@PathVariable("tmId") Long tmId) {
        return trademarkService.getById(tmId);
    }
    /**
     * 获取全部分类信息
     * @return
     */
    @GetMapping("/getBaseCategoryList")
    @ApiOperation("获取全部分类信息")
    public List<JSONObject> getBaseCategoryList() {
        return dataViewService.getBaseCategoryList();
    }

    /**
     *  根据spuId 查询sku的销售属性值，使用Map封装返回数据
     */
    @GetMapping("/getSkuValueIdsMap/{spuId}")
    @ApiOperation("根据spuId 查询sku的销售属性值，使用Map封装返回数据")
    public Map<Object, Object> getSkuValueIdsMap(@PathVariable("spuId") Long spuId) {
        return skuManagerService.getSkuValueIdsMap(spuId);
    }

    /**
     * 通过skuId获取sku对应的平台属性
     */
    @GetMapping("/getAttrList/{skuId}")
    @ApiOperation("通过skuId获取sku对应的平台属性")
    public List<BaseAttrInfo> getAttrList(@PathVariable("skuId") Long skuId) {
        return skuManagerService.getAttrList(skuId);
    }

    /**
     * 根据spuid获取商品海报
     */
    @GetMapping("/findSpuPosterBySpuId/{spuId}")
    @ApiOperation("根据spuid获取商品海报")
    public List<SpuPoster> findSpuPosterBySpuId(@PathVariable("spuId") Long spuId) {
        return spuManagerService.findSpuPosterBySpuId(spuId);
    }

    /**
     * 根据skuId和spuId查询商品销售属性组合
     * @param skuId
     * @param spuId            v
     * @return
     */
    @GetMapping("/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    @ApiOperation("根据skuId和spuId查询商品销售属性组合")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("skuId") Long skuId, @PathVariable("spuId") Long spuId) {
        return skuManagerService.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    /**
     * 根据skuId查询sku价格
     * @param skuId
     * @return
     */
    @GetMapping("/getSkuPrice/{skuId}")
    @ApiOperation("根据skuId查询sku价格")
    public BigDecimal getSkuPrice(@PathVariable("skuId") Long skuId) {
        return skuManagerService.getSkuPrice(skuId);
    }

    /**
     * 根据三级分类id查询分类视图
     * @param category3Id
     * @return
     */
    @GetMapping("/getCategoryView/{category3Id}")
    @ApiOperation("根据三级分类id查询分类视图")
    public BaseCategoryView getCategoryView(@PathVariable("category3Id") Long category3Id) {
        return dataViewService.getBaseCategoryView(category3Id);
    }

    /**
     * 根据skuId查询sku信息
     * @param skuId
     * @return
     */
    @GetMapping("/{skuId}")
    @ApiOperation("根据skuId查询sku信息")
    public SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId) {
        return skuManagerService.getSkuInfo(skuId);
    }

}
