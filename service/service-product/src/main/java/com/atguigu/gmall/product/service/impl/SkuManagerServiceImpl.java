package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.enums.SaleStatus;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.SkuManagerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@SuppressWarnings("all")
public class SkuManagerServiceImpl implements SkuManagerService {

    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;


    /**
     * 上架/下架sku
     *
     * @param skuId
     * @deprecated 是否销售（1：是 0：否）
     */
    @Override
    public void skuSaleStatusChange(Long skuId, Integer isSale) {
        skuInfoMapper.update(null,
                Wrappers.<SkuInfo>lambdaUpdate()
                        .set(SkuInfo::getIsSale, isSale)
                        .eq(SkuInfo::getId, skuId));
    }


    /**
     * 分页查询sku列表
     *
     * @param page
     * @return
     */
    @Override
    public IPage<SkuInfo> getSkuListPage(Page<SkuInfo> page) {
        LambdaQueryWrapper<SkuInfo> lambdaQueryWrapper = Wrappers.<SkuInfo>lambdaQuery()
                .orderByDesc(SkuInfo::getSpuId)
                .orderByDesc(SkuInfo::getPrice);
        Page<SkuInfo> skuInfoPage = skuInfoMapper.selectPage(page, lambdaQueryWrapper);
        skuInfoPage.getRecords().forEach(skuInfo -> {
            BaseTrademark baseTrademark = baseTrademarkMapper.selectById(skuInfo.getTmId());
            skuInfo.setTmName(baseTrademark == null ? "未知品牌" : baseTrademark.getTmName());
        });
        return skuInfoPage;
    }

    /**
     * 保存sku信息
     *
     * @param skuInfo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuInfo(SkuInfo skuInfo) {
        // 保存skuInfo
        skuInfoMapper.insert(skuInfo);
        // 保存sku图片信息
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (CollectionUtils.isNotEmpty(skuImageList)) {
            skuImageList.forEach(skuImage -> {
                skuImage.setSkuId(skuInfo.getId());
            });
            skuImageMapper.insertBatch(skuImageList);
        }
        // 保存sku平台属性关联信息
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (CollectionUtils.isNotEmpty(skuAttrValueList)) {
            skuAttrValueList.forEach(skuAttrValue -> {
                skuAttrValue.setSkuId(skuInfo.getId());
            });
            skuAttrValueMapper.insertBatch(skuAttrValueList);
        }
        // 保存sku销售属性信息
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (CollectionUtils.isNotEmpty(skuSaleAttrValueList)) {
            skuSaleAttrValueList.forEach(skuSaleAttrValue -> {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
            });
            skuSaleAttrValueMapper.insertBatch(skuSaleAttrValueList);
        }
    }


}
