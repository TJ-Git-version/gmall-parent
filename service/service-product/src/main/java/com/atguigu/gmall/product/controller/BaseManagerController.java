package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.product.service.BaseManagerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/product")
@RequiredArgsConstructor
@Api("基本信息管理的接口")
// @CrossOrigin
@SuppressWarnings("all")
public class BaseManagerController {

    private final BaseManagerService baseManagerService;

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
    @GetMapping("/getCategory2")
    @ApiOperation("获取二级分类列表")
    public Result<List<BaseCategory2>> getCategory2List(Long category1Id) {
        return Result.ok(baseManagerService.getCategory2List(category1Id));
    }

    /**
     * 获取三级分类列表
     */
    @GetMapping("/getCategory3")
    @ApiOperation("获取三级分类列表")
    public Result<List<BaseCategory3>> getCategory3List(Long category2Id) {
        return Result.ok(baseManagerService.getCategory3List(category2Id));
    }


    /**
     * 根据分类Id 获取平台属性数据
     * 接口说明：
     *      1，平台属性可以挂在一级分类、二级分类和三级分类
     *      2，查询一级分类下面的平台属性，传：category1Id，0，0；   取出该分类的平台属性
     *      3，查询二级分类下面的平台属性，传：category1Id，category2Id，0；
     *         取出对应一级分类下面的平台属性与二级分类对应的平台属性
     *      4，查询三级分类下面的平台属性，传：category1Id，category2Id，category3Id；
     *         取出对应一级分类、二级分类与三级分类对应的平台属性
     * @param category1Id 一级分类id
     * @param category2Id 二级分类id
     * @param category3Id 三级分类id
     * @return 平台属性数据
     */
    @GetMapping("/attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result<List<BaseAttrInfo>> getAttrInfoList(@PathVariable("category1Id") Long category1Id,
                                                       @PathVariable("category2Id") Long category2Id,
                                                       @PathVariable("category3Id") Long category3Id) {
        return Result.ok(baseManagerService.getAttrInfoList(category1Id, category2Id, category3Id));
    }
}
