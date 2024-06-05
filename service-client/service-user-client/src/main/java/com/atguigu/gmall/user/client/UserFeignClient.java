package com.atguigu.gmall.user.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.client.impl.UserDegradeFeignClient;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@FeignClient(value = "service-user", path = "/api/user", fallback = UserDegradeFeignClient.class)
public interface UserFeignClient {

    /**
     * 根据用户id查询用户信息
     */
    @GetMapping("/inner/findUserAddressListByUserId/{userId}")
    @ApiOperation("根据用户id查询用户信息")
    public List<UserAddress> findUserAddressListByUserId(@PathVariable String userId);

}
