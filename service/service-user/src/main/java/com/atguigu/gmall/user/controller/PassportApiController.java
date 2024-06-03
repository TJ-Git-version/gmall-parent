package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserInfoService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/user/passport")
@RequiredArgsConstructor
public class PassportApiController {

    private final UserInfoService userInfoService;

    /**
     * 用户登录
     * @param userInfo
     * @param request
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<Map<String, Object>> login(@RequestBody UserInfo userInfo, HttpServletRequest request) {
        Map<String, Object> resultLogin = userInfoService.login(userInfo, request);
        return Result.ok(resultLogin);
    }


    /**
     * 退出登录
     */
    @GetMapping("/logout")
    @ApiOperation("退出登录")
    public Result<Void> logout(HttpServletRequest request) {
        userInfoService.logout(request);
        return Result.ok();
    }

}
