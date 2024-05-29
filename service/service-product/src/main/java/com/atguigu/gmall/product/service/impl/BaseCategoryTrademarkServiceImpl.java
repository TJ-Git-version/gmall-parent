package com.atguigu.gmall.product.service.impl;
import com.google.common.collect.Lists;

import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.atguigu.gmall.product.mapper.BaseCategoryTrademarkMapper;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseCategoryTrademarkService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("all")
public class BaseCategoryTrademarkServiceImpl extends ServiceImpl<BaseCategoryTrademarkMapper, BaseCategoryTrademark> implements BaseCategoryTrademarkService {


    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    /**
     * 根据三级分类id查询品牌分类列表
     * @param category3Id
     * @return
     */
    @Override
    public List<BaseTrademark> findTrademarkList(Long category3Id) {
        return baseMapper.findTrademarkList(category3Id);
    }

    /**
     * 根据三级分类id查询当前品牌列表
     * @param category3Id
     * @return
     */
    @Override
    public List<BaseTrademark> findCurrentTrademarkList(Long category3Id) {
        List<BaseTrademark> baseTrademarklist=Lists.newArrayList();
        // 查询品牌分类关联表
        List<BaseCategoryTrademark> baseCategoryTrademarkList = baseMapper.selectList(Wrappers.<BaseCategoryTrademark>lambdaQuery().eq(BaseCategoryTrademark::getCategory3Id, category3Id));
        // 如果品牌分类关联表为空，则返回null
        if(CollectionUtils.isEmpty(baseCategoryTrademarkList)) {
            return null;
        }
        // 筛选当前品牌分类下已关联的品牌id
        List<Long> trademarkIdList = baseCategoryTrademarkList.stream().map(BaseCategoryTrademark::getTrademarkId).collect(Collectors.toList());
        // 查询当前品牌分类下没有被关联的品牌
        List<BaseTrademark> baseTrademarkList = baseTrademarkMapper.selectList(Wrappers.<BaseTrademark>lambdaQuery().notIn(BaseTrademark::getId, trademarkIdList));
        // 或使用lambda表达式
        // List<BaseTrademark>  baseTrademarkList = baseTrademarkMapper.selectList(null);
        // baseTrademarkList = baseTrademarkList.stream().filter(baseTrademark -> {
        //     // 筛选当前品牌分类下没有被关联的品牌
        //     return !trademarkIdList.contains(baseTrademark.getId());
        // }).collect(Collectors.toList());
        return baseTrademarkList;
    }

    @Override
    public void saveCategoryTrademark(CategoryTrademarkVo categoryTrademarkVo) {
        if(categoryTrademarkVo.getCategory3Id() == null || CollectionUtils.isEmpty(categoryTrademarkVo.getTrademarkIdList())) {
            throw new GmallException(ResultCodeEnum.FAIL);
        }
        // List<BaseCategoryTrademark> baseCategoryTrademarkList = new ArrayList<>();
        // Long category3Id = categoryTrademarkVo.getCategory3Id();
        // categoryTrademarkVo.getTrademarkIdList().forEach(trademarkId -> {
        //     BaseCategoryTrademark baseCategoryTrademark = new BaseCategoryTrademark();
        //     baseCategoryTrademark.setCategory3Id(category3Id);
        //     baseCategoryTrademark.setTrademarkId(trademarkId);
        //     baseCategoryTrademarkList.add(baseCategoryTrademark);
        // });
        hasTrademarkIdExist(categoryTrademarkVo);
        List<BaseCategoryTrademark> baseCategoryTrademarkList = categoryTrademarkVo.getTrademarkIdList().stream().map(trademarkId -> {
            BaseCategoryTrademark baseCategoryTrademark = new BaseCategoryTrademark();
            baseCategoryTrademark.setCategory3Id(categoryTrademarkVo.getCategory3Id());
            baseCategoryTrademark.setTrademarkId(trademarkId);
            return baseCategoryTrademark;
        }).collect(Collectors.toList());
        this.saveBatch(baseCategoryTrademarkList);
    }

    private void hasTrademarkIdExist(CategoryTrademarkVo categoryTrademarkVo) {
        Integer resultCount = baseMapper.selectCount(Wrappers.<BaseCategoryTrademark>lambdaQuery().in(BaseCategoryTrademark::getTrademarkId, categoryTrademarkVo.getTrademarkIdList()));
        if(resultCount > 0) {
            throw new GmallException("品牌分类已存在，请勿重复添加");
        }
    }


}
