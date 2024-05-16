package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.mapper.SupInfoMapper;
import com.atguigu.gmall.product.service.SpuInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("all")
public class SpuInfoServiceImpl implements SpuInfoService {

    @Autowired
    private SupInfoMapper spuInfoMapper;

    /**
     * 根据三级分类id获取spu信息列表
     *
     * @param page
     * @param category3Id
     * @return
     */
    @Override
    public IPage<SpuInfo> getSpuInfosPage(Page<SpuInfo> page, Long category3Id) {
        return spuInfoMapper.selectPage(page, Wrappers.<SpuInfo>lambdaQuery().eq(SpuInfo::getCategory3Id, category3Id));
    }

}
