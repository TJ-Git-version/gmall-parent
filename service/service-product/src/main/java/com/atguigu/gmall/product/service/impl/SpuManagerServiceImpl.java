package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.SpuManagerService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@SuppressWarnings("all")
public class SpuManagerServiceImpl implements SpuManagerService {

    @Autowired
    private SupInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuPosterMapper spuPosterMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;



    @Override
    @GmallCache(prefix = "spuPoster:")
    public List<SpuPoster> findSpuPosterBySpuId(Long spuId) {
        return spuPosterMapper.selectList(Wrappers.<SpuPoster>lambdaQuery().eq(SpuPoster::getSpuId, spuId));
    }

    /**
     * 根据spuId获取spu图片列表
     * @param spuId
     * @return
     */
    @Override
    @GmallCache(prefix = "spuImages:")
    public List<SpuImage> getSpuImageList(Long spuId) {
        return spuImageMapper.selectList(Wrappers.<SpuImage>lambdaQuery().eq(SpuImage::getSpuId, spuId));
    }


    /**
     * 根据spuId获取spu信息
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        // 使用mybatis查询
        return spuSaleAttrMapper.getSpuSaleAttrList(spuId);

        // 使用mybatis-plus查询
        // List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrMapper.selectList(Wrappers.<SpuSaleAttr>lambdaQuery().eq(SpuSaleAttr::getSpuId, spuId));
        // spuSaleAttrs.forEach(spuSaleAttr -> {
        //     LambdaQueryWrapper<SpuSaleAttrValue> lambdaQueryWrapper = Wrappers.<SpuSaleAttrValue>lambdaQuery()
        //             .eq(SpuSaleAttrValue::getSpuId, spuId)
        //             .eq(SpuSaleAttrValue::getBaseSaleAttrId, spuSaleAttr.getBaseSaleAttrId());
        //     spuSaleAttr.setSpuSaleAttrValueList(spuSaleAttrValueMapper.selectList(lambdaQueryWrapper));
        // });
        // return spuSaleAttrs;
    }


    /**
     * 保存spu信息
     * @param spuInfo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSpuInfo(SpuInfo spuInfo) {
        // 保存spu信息
        spuInfoMapper.insert(spuInfo);
        // 保存spu图片信息
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (CollectionUtils.isNotEmpty(spuImageList)) {
            spuImageList.forEach(spuImage -> {
                spuImage.setSpuId(spuInfo.getId());
            });
            spuImageMapper.insertBatch(spuImageList);
        }
        // 保存spu海报信息
        List<SpuPoster> spuPosterList = spuInfo.getSpuPosterList();
        if (CollectionUtils.isNotEmpty(spuPosterList)) {
            spuPosterList.forEach(spuPoster -> {
                spuPoster.setSpuId(spuInfo.getId());
            });
            spuPosterMapper.insertBatch(spuPosterList);
        }
        // 保存销售属性信息
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (CollectionUtils.isNotEmpty(spuSaleAttrList)) {
            spuSaleAttrList.forEach(spuSaleAttr -> {
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insert(spuSaleAttr);
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                // 保存销售属性值信息
                if (CollectionUtils.isNotEmpty(spuSaleAttrValueList)) {
                    spuSaleAttrValueList.forEach(spuSaleAttrValue -> {
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    });
                }
            });
        }
    }


    /**
     * 根据三级分类id获取spu信息列表
     *
     * @param page
     * @param category3Id
     * @return
     */
    @Override
    public IPage<SpuInfo> getSpuInfosPage(Page<SpuInfo> page, Long category3Id) {
        Page<SpuInfo> spuInfoPage = spuInfoMapper.selectPage(page, Wrappers.<SpuInfo>lambdaQuery().eq(SpuInfo::getCategory3Id, category3Id));
        spuInfoPage.getRecords().forEach(spuInfo -> {
            BaseTrademark baseTrademark = baseTrademarkMapper.selectById(spuInfo.getTmId());
            spuInfo.setTmName(baseTrademark == null ? "未知品牌" : baseTrademark.getTmName());
        });
        return spuInfoPage;
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectList(Wrappers.emptyWrapper());
    }



}
