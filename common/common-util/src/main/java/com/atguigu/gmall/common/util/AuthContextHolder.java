package com.atguigu.gmall.common.util;

//import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 获取登录用户信息类
 */
public class AuthContextHolder {
    /**
     * 获取当前登录用户id
     * @return
     */
    public static String getUserId() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String userId = "";
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            userId = request.getHeader("userId");
        }
        return StringUtils.isEmpty(userId) ? "" : userId;
    }

    /**
     * 获取当前登录用户id
     * @param request
     * @return
     */
    public static String getUserId(HttpServletRequest request) {
        String userId = request.getHeader("userId");
        return StringUtils.isEmpty(userId) ? "" : userId;
    }

    /**
     * 获取当前未登录临时用户id
     * @param request
     * @return
     */
    public static String getUserTempId(HttpServletRequest request) {
        String userTempId = request.getHeader("userTempId");
        return StringUtils.isEmpty(userTempId) ? "" : userTempId;
    }
}
