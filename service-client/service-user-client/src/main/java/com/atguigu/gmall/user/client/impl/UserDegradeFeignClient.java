package com.atguigu.gmall.user.client.impl;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UserDegradeFeignClient implements UserFeignClient {

    /**
     * 根据用户id查询用户地址列表
     * @param userId
     * @return
     */
    @Override
    public List<UserAddress> findUserAddressListByUserId(String userId) {
        return Collections.emptyList();
    }
}
