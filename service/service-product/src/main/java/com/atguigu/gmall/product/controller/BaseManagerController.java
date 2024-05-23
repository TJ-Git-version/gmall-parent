package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.BaseManagerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/product")
@RequiredArgsConstructor
@Api(tags = "商品基础属性接口")
// @CrossOrigin
@SuppressWarnings("all")
public class BaseManagerController {

    private final BaseManagerService baseManagerService;

    /**
     * 根据属性id获取属性值列表
     * @param baseAttrInfo
     * @return
     */
    @GetMapping("/getAttrValueList/{attrId}")
    @ApiOperation("根据属性id获取属性值列表")
    public Result<List<BaseAttrValue>> getAttrValueList(@ApiParam(value = "属性id") @PathVariable("attrId") Long attrId) {
        BaseAttrInfo baseAttrInfo = baseManagerService.getBaseAttrInfo(attrId);
        return Result.ok(baseAttrInfo.getAttrValueList());
    }
    /**
     * 保存和修改平台属性方法
     * @param baseAttrInfo
     * @return
     */
    @PostMapping("/saveAttrInfo")
    @ApiOperation("保存和修改平台属性方法")
    public Result<Void> saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo) {
        baseManagerService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }

    /**
     * 获取一级分类列表
     * @return
     */
    @GetMapping("/getCategory1")
    @ApiOperation("获取一级分类列表")
    public Result<List<BaseCategory1>> getCategory1List(){
        return Result.ok(baseManagerService.getCategory1List());
    }

    /**
     * 获取二级分类列表
     */
    @GetMapping("/getCategory2/{category1Id}")
    @ApiOperation("获取二级分类列表")
    public Result<List<BaseCategory2>> getCategory2List(@PathVariable Long category1Id) {
        return Result.ok(baseManagerService.getCategory2List(category1Id));
    }

    /**
     * 获取三级分类列表
     */
    @GetMapping("/getCategory3/{category2Id}")
    @ApiOperation("获取三级分类列表")
    public Result<List<BaseCategory3>> getCategory3List(@PathVariable Long category2Id) {
        return Result.ok(baseManagerService.getCategory3List(category2Id));
    }


    /**
     * 根据分类Id获取平台属性数据
     * 接口说明：
     *      1：平台属性可以挂在一级分类、二级分类和三级分类
     *      2：查询一级分类下面的平台属性，传：category1Id，0，0；   取出该分类的平台属性
     *      3：查询二级分类下面的平台属性，传：category1Id，category2Id，0；
     *         取出对应一级分类下面的平台属性与二级分类对应的平台属性
     *      4：查询三级分类下面的平台属性，传：category1Id，category2Id，category3Id；
     *         取出对应一级分类、二级分类与三级分类对应的平台属性
     * @param category1Id 一级分类id
     * @param category2Id 二级分类id
     * @param category3Id 三级分类id
     * @return 平台属性数据
     */
    @GetMapping("/attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    @ApiOperation(value = "根据分类Id获取平台属性数据")
    public Result<List<BaseAttrInfo>> getAttrInfoList(@ApiParam(value = "一级分类id") @PathVariable("category1Id") Long category1Id,
                                                      @ApiParam(value = "二级分类id") @PathVariable("category2Id") Long category2Id,
                                                      @ApiParam(value = "三级分类id") @PathVariable("category3Id") Long category3Id) {
        return Result.ok(baseManagerService.getAttrInfoList(category1Id, category2Id, category3Id));
    }
}
