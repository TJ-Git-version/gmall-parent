package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.BaseManagerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * 根据属性id获取属性值列表
     *
     * @param attrId
     * @return
     */
    @Override
    public BaseAttrInfo getBaseAttrInfo(Long attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);
        if (baseAttrInfo != null) {
            List<BaseAttrValue> baseAttrValueList = getAttrValueList(attrId);
            baseAttrInfo.setAttrValueList(baseAttrValueList);
        }
        return baseAttrInfo;
    }

    /**
     * 根据属性id获取属性值列表
     * @param attrId
     * @return
     */
    private List<BaseAttrValue> getAttrValueList(Long attrId) {
        return baseAttrValueMapper.selectList(Wrappers.<BaseAttrValue>lambdaQuery().eq(BaseAttrValue::getAttrId, attrId));
    }

    /**
     * 获取平台属性列表
     *
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
     * 保存和修改平台属性方法
     *
     * @param baseAttrInfo 平台属性信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        Long id = baseAttrInfo.getId();
        BaseAttrInfo isBaseAttrInfo = baseAttrInfoMapper.selectById(id);
        // 判断当前操作是新增还是修改
        if (isBaseAttrInfo != null) {
            baseAttrInfoMapper.updateById(baseAttrInfo);
            // 删除原有属性值
            baseAttrValueMapper.delete(Wrappers.<BaseAttrValue>lambdaQuery().eq(BaseAttrValue::getAttrId, id));
        } else {
            baseAttrInfoMapper.insert(baseAttrInfo);
        }

        // 保存平台属性值
        List<BaseAttrValue> baseAttrValueList = baseAttrInfo.getAttrValueList();
        if (CollectionUtils.isNotEmpty(baseAttrValueList)) {
            baseAttrValueList.forEach(baseAttrValue -> {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            });
        }
    }


    /**
     * 获取一级分类列表
     *
     * @return
     */
    @Override
    public List<BaseCategory1> getCategory1List() {
        return baseCategory1Mapper.selectList(null);
    }

    /**
     * 获取二级分类列表
     *
     * @param category1Id
     * @return
     */
    @Override
    public List<BaseCategory2> getCategory2List(Long category1Id) {
        return baseCategory2Mapper.selectList(Wrappers.<BaseCategory2>lambdaQuery().eq(BaseCategory2::getCategory1Id, category1Id));
    }

    /**
     * 获取三级分类列表
     *
     * @param category2Id
     * @return
     */
    @Override
    public List<BaseCategory3> getCategory3List(Long category2Id) {
        return baseCategory3Mapper.selectList(Wrappers.<BaseCategory3>lambdaQuery().eq(BaseCategory3::getCategory2Id, category2Id));
    }


}
