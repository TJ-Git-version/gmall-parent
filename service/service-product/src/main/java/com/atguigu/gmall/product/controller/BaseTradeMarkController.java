package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 品牌列表管理
 */
@RestController
@RequestMapping("/admin/product/baseTrademark")
@RequiredArgsConstructor
@Api(tags = "品牌列表管理")
public class BaseTradeMarkController {

    private final BaseTrademarkService baseTrademarkService;

    /**
     * 根据分页获取商品品牌列表
     * @param current
     * @param limit
     * @return
     */
    @GetMapping("/{current}/{limit}")
    public Result<IPage<BaseTrademark>> getTradeMarkList(@PathVariable Long current,
                                                         @PathVariable Long limit){
        Page<BaseTrademark> page = new Page<>(current, limit);
        return Result.ok(baseTrademarkService.getTradeMarkList(page));
    }

    /**
     * 根据id获取品牌详情
     * @param id
     * @return
     */
    @GetMapping("/get/{id}")
    public Result<BaseTrademark> getTradeMarkById(@PathVariable Long id){
        return Result.ok(baseTrademarkService.getById(id));
    }
    /**
     * 新增品牌
     * @param baseTrademark
     * @return
     */
    @PostMapping("/save")
    public Result<Void> save(@RequestBody BaseTrademark baseTrademark){
        baseTrademarkService.save(baseTrademark);
        return Result.ok();
    }

    /**
     * 修改品牌
     * @param baseTrademark
     * @return
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody BaseTrademark baseTrademark) {
        baseTrademarkService.updateById(baseTrademark);
        return Result.ok();
    }

    /**
     * 删除品牌
     * @param id
     * @return
     */
    @DeleteMapping("/remove/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        baseTrademarkService.removeById(id);
        return Result.ok();
    }
}
