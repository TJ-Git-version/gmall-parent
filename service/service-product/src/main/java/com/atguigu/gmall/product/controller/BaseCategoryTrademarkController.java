package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.atguigu.gmall.product.service.BaseCategoryTrademarkService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/product/baseCategoryTrademark")
@RequiredArgsConstructor
@Api(tags = "分类品牌关系管理")
public class BaseCategoryTrademarkController {

    private final BaseCategoryTrademarkService baseCategoryTrademarkService;

    /**
     * 根据三级分类id查询分类品牌列表
     * @param category3Id
     * @return
     */
    @GetMapping("/findTrademarkList/{category3Id}")
    @ApiOperation(value = "根据三级分类id查询分类品牌列表")
    public Result<List<BaseTrademark>> findTrademarkList(@PathVariable("category3Id") Long category3Id){
        List<BaseTrademark> baseTrademarkList = baseCategoryTrademarkService.findTrademarkList(category3Id);
        return Result.ok(baseTrademarkList);
    }

    ///admin/product/baseCategoryTrademark/findCurrentTrademarkList/61
    @GetMapping("/findCurrentTrademarkList/{category3Id}")
    @ApiOperation(value = "根据三级分类id查询当前品牌列表")
    public Result<List<BaseTrademark>> findCurrentTrademarkList(@PathVariable("category3Id") Long category3Id){
        List<BaseTrademark> baseTrademarkList = baseCategoryTrademarkService.findCurrentTrademarkList(category3Id);
        return Result.ok(baseTrademarkList);
    }

    //TODO: 新增、删除
    @PostMapping("/save")
    @ApiOperation(value = "新增分类品牌关系")
    public Result<Void> save(@RequestBody CategoryTrademarkVo categoryTrademarkVo){
        baseCategoryTrademarkService.saveCategoryTrademark(categoryTrademarkVo);
        return Result.ok();
    }
    @DeleteMapping("/remove/{category3Id}/{trademarkId}")
    @ApiOperation(value = "删除分类品牌关系")
    public Result<Void> remove(@PathVariable("category3Id") Long category3Id, @PathVariable("trademarkId") Long trademarkId) {
        baseCategoryTrademarkService.remove(Wrappers.<BaseCategoryTrademark>lambdaQuery().eq(BaseCategoryTrademark::getCategory3Id, category3Id).eq(BaseCategoryTrademark::getTrademarkId, trademarkId));
        return Result.ok();
    }
}
