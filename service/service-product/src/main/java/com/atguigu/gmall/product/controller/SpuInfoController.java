package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.SpuInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/product")
@Api(tags = "后台管理-商品管理")
@RequiredArgsConstructor
public class SpuInfoController {

    private final SpuInfoService spuInfoService;

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
        return Result.ok(spuInfoService.getSpuInfosPage(page,category3Id));
    }

}
