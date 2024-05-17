package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.SpuManagerService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/product")
@Api(tags = "后台管理-商品管理")
@RequiredArgsConstructor
public class SpuManagerController {

    private final SpuManagerService spuManagerService;

    /**
     * 根据三级分类id获取spu信息列表
     * @param category3Id
     * @return
     */
    //
    @GetMapping("/{current}/{limit}")
    @ApiOperation("根据三级分类id获取spu信息列表")
    public Result<IPage<SpuInfo>> getSpuInfosPage(@PathVariable Long current,
                                                  @PathVariable Long limit,
                                                  Long category3Id) {
        // 服装Page
        Page<SpuInfo> page = new Page<>(current, limit);
        return Result.ok(spuManagerService.getSpuInfosPage(page,category3Id));
    }

    /**
     * 查询基本销售属性
     * @return
     */
    @GetMapping("/baseSaleAttrList")
    @ApiOperation("查询基本销售属性")
    public Result<List<BaseSaleAttr>> getBaseSaleAttrList() {
        return Result.ok(spuManagerService.getBaseSaleAttrList());
    }

    /**
     * 保存spu信息
     */
    @ApiOperation("保存spu信息")
    @PostMapping("/saveSpuInfo")
    public Result<Void> saveSpuInfo(@RequestBody SpuInfo spuInfo) {
        spuManagerService.saveSpuInfo(spuInfo);
        return Result.ok();
    }

    /**
     * 根据spuId查询spu销售属性列表
     * @param spuId
     */
    @GetMapping("/spuSaleAttrList/{spuId}")
    public Result<List<SpuSaleAttr>> getSpuSaleAttrList(@PathVariable Long spuId) {
        return Result.ok(spuManagerService.getSpuSaleAttrList(spuId));
    }

    /**
     * 根据spuId查询spu图片列表
     * @param spuId
     */
    @GetMapping("/spuImageList/{spuId}")
    public Result<List<SpuImage>> getSpuImageList(@PathVariable Long spuId) {
        return Result.ok(spuManagerService.getSpuImageList(spuId));
    }

}
