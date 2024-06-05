package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.service.UserAddressService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@SuppressWarnings("all")
@Slf4j
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {

    /**
     * 根据用户id查询用户地址列表
     * @param userId
     * @return
     */
    @Override
    public List<UserAddress> findUserAddressListByUserId(String userId) {
        return baseMapper.selectList(Wrappers.<UserAddress>lambdaQuery().eq(UserAddress::getUserId, userId));
    }
}
