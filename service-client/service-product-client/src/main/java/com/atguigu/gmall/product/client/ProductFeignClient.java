package com.atguigu.gmall.product.client;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.impl.ProductDegradeFeignClient;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品详情Feign客户端
 * value:指定要调用的微服务名称
 * fallback:指定熔断器的类
 */
@FeignClient(value = "service-product", fallback = ProductDegradeFeignClient.class)
public interface ProductFeignClient {

    /**
     * 根据品牌id查询品牌信息
     * @param tmId
     * @return
     */
    @GetMapping("/api/product/inner/getTrademark/{tmId}")
    @ApiOperation("根据品牌id查询品牌信息")
    public BaseTrademark getTrademark(@PathVariable("tmId") Long tmId);
    /**
     * 获取全部分类信息
     * @return
     */
    @GetMapping("/api/product/inner/getBaseCategoryList")
    @ApiOperation("获取全部分类信息")
    public List<JSONObject> getBaseCategoryList();

    /**
     *  根据spuId 查询sku的销售属性值，使用Map封装返回数据
     */
    @GetMapping("/api/product/inner/getSkuValueIdsMap/{spuId}")
    @ApiOperation("根据spuId 查询sku的销售属性值，使用Map封装返回数据")
    public Map<Object, Object> getSkuValueIdsMap(@PathVariable("spuId") Long spuId) ;

    /**
     * 通过skuId获取sku对应的平台属性
     */
    @GetMapping("/api/product/inner/getAttrList/{skuId}")
    @ApiOperation("通过skuId获取sku对应的平台属性")
    public List<BaseAttrInfo> getAttrList(@PathVariable("skuId") Long skuId);

    /**
     * 根据spuid获取商品海报
     */
    @GetMapping("/api/product/inner/findSpuPosterBySpuId/{spuId}")
    @ApiOperation("根据spuid获取商品海报")
    public List<SpuPoster> findSpuPosterBySpuId(@PathVariable("spuId") Long spuId);

    /**
     * 根据skuId和spuId查询商品销售属性组合
     * @param skuId
     * @param spuId
     * @return
     */
    @GetMapping("/api/product/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    @ApiOperation("根据skuId和spuId查询商品销售属性组合")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("skuId") Long skuId, @PathVariable("spuId") Long spuId);
    /**
     * 根据skuId查询sku价格
     * @param skuId
     * @return
     */
    @GetMapping("/api/product/inner/getSkuPrice/{skuId}")
    @ApiOperation("根据skuId查询sku价格")
    public BigDecimal getSkuPrice(@PathVariable("skuId") Long skuId);

    /**
     * 根据三级分类id查询分类视图
     * @param category3Id
     * @return
     */
    @GetMapping("/api/product/inner/getCategoryView/{category3Id}")
    @ApiOperation("根据三级分类id查询分类视图")
    public BaseCategoryView getCategoryView(@PathVariable("category3Id") Long category3Id);

    /**
     * 根据skuId查询sku信息
     * @param skuId
     * @return
     */
    @GetMapping("/api/product/inner/{skuId}")
    @ApiOperation("根据skuId查询sku信息")
    public SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId);

}
