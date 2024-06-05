package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserAddressService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApiController {

    private final UserAddressService userAddressService;

    /**
     * 获取用户地址信息
     */
    @GetMapping("/inner/findUserAddressListByUserId/{userId}")
    @ApiOperation("获取用户地址信息")
    public List<UserAddress> findUserAddressListByUserId(@PathVariable String userId) {
        return userAddressService.findUserAddressListByUserId(userId);
    }

}
