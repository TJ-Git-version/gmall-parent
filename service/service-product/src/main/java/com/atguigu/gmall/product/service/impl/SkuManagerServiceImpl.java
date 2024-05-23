package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.enums.SaleStatus;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.SkuManagerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;


    @Override
    public Map<Object, Object> getSkuValueIdsMap(Long spuId) {
        List<Map<Object, Object>> skuValueIdsMap = skuSaleAttrValueMapper.getSkuValueIdsMap(spuId);
        Map<Object, Object> resultMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(skuValueIdsMap)) {
            skuValueIdsMap.forEach(skuValueIdMap -> {
                    resultMap.put(skuValueIdMap.get("value_ids"), skuValueIdMap.get("sku_id"));
            });
        }
        return resultMap;
    }

    /**
     * 通过skuId获取sku对应的平台属性
     *
     * @param skuId
     * @return
     */
    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        return baseAttrInfoMapper.getAttrList(skuId);
    }


    /**
     * 根据skuId和spuId查询商品销售属性组合
     *
     * @param skuId
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return spuSaleAttrMapper.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }


    /**
     * 根据skuId查询sku价格
     *
     * @param skuId
     * @return
     */
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (ObjectUtils.isNotNull(skuInfo)) {
            return skuInfo.getPrice();
        }
        return new BigDecimal("0");
    }


    /**
     * 根据skuId查询sku信息
     *
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        // 查询sku基本信息
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (ObjectUtils.isNotNull(skuInfo)) {
            // 查询sku图片信息
            List<SkuImage> skuImageList = skuImageMapper.selectList(Wrappers.<SkuImage>lambdaQuery().eq(SkuImage::getSkuId, skuId));
            skuInfo.setSkuImageList(skuImageList);
        }
        return skuInfo;
    }


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
