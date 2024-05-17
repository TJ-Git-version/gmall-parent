package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.enums.SaleStatus;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.service.SkuManagerService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/product")
@RequiredArgsConstructor
public class SkuManagerController {

    private final SkuManagerService skuManagerService;

    /**
     * 下架商品
     * @param skuId
     * @return
     * @deprecated 是否销售（1：是 0：否）
     */
    @GetMapping("/cancelSale/{skuId}")
    @ApiOperation("下架商品")
    public Result<Void> cancelSale(@PathVariable("skuId") Long skuId) {
        skuManagerService.skuSaleStatusChange(skuId, SaleStatus.CANCEL_SALE.getStatus());
        return Result.ok();
    }

    /**
     * 上架商品
     * @param skuId
     * @return
     * @deprecated 是否销售（1：是 0：否）
     */
    @GetMapping("/onSale/{skuId}")
    @ApiOperation("上架商品")
    public Result<Void> onSale(@PathVariable("skuId") Long skuId) {
        skuManagerService.skuSaleStatusChange(skuId, SaleStatus.ON_SALE.getStatus());
        return Result.ok();
    }

    /**
     * 分页查询sku信息
     * @param current
     * @param limit
     * @return
     */
    @GetMapping("/list/{current}/{limit}")
    @ApiOperation("分页查询sku信息")
    public Result<IPage<SkuInfo>> list(@PathVariable("current") Integer current, @PathVariable("limit") Integer limit) {
        Page<SkuInfo> page = new Page<>(current, limit);
        IPage<SkuInfo> skuListPage = skuManagerService.getSkuListPage(page);
        return Result.ok(skuListPage);
    }

    /**
     * 保存sku信息
     * @param skuInfo
     * @return
     */
    @PostMapping("/saveSkuInfo")
    @ApiOperation("保存sku信息")
    public Result<Void> saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        skuManagerService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

}
