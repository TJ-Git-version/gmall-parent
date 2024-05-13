package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.BaseManagerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BaseManagerServiceImpl implements BaseManagerService {

    private final BaseCategory1Mapper baseCategory1Mapper;
    private final BaseCategory2Mapper baseCategory2Mapper;
    private final BaseCategory3Mapper baseCategory3Mapper;
    private final BaseAttrInfoMapper baseAttrInfoMapper;
    private final BaseAttrValueMapper baseAttrValueMapper;

    /**
     * 获取平台属性列表
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        return baseAttrInfoMapper.selectAttrInfoList(category1Id, category2Id, category3Id);
    }

    /**
     * 获取一级分类列表
     * @return
     */
    @Override
    public List<BaseCategory1> getCategory1List() {
        return baseCategory1Mapper.selectList(null);
    }

    /**
     * 获取二级分类列表
     * @param category1Id
     * @return
     */
    @Override
    public List<BaseCategory2> getCategory2List(Long category1Id) {
        LambdaQueryWrapper<BaseCategory2> lqw = Wrappers.lambdaQuery();
        lqw.eq(BaseCategory2::getCategory1Id, category1Id);
        return baseCategory2Mapper.selectList(lqw);
    }

    /**
     * 获取三级分类列表
     * @param category2Id
     * @return
     */
    @Override
    public List<BaseCategory3> getCategory3List(Long category2Id) {
        LambdaQueryWrapper<BaseCategory3> lqw = Wrappers.lambdaQuery();
        lqw.eq(BaseCategory3::getCategory2Id, category2Id);
        return baseCategory3Mapper.selectList(lqw);
    }


}
