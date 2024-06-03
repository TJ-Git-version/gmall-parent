package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface UserInfoService {

    /**
     * 用户登录
     * @param userInfo
     * @param request
     * @return
     */
    Map<String, Object> login(UserInfo userInfo, HttpServletRequest request);

    /**
     * 退出登录
     * @param request
     */
    void logout(HttpServletRequest request);

}
