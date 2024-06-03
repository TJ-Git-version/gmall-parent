package com.atguigu.gmall.user.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserInfoService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@SuppressWarnings("all")
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 退出登录
     * @param request
     */
    @Override
    public void logout(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null) {
            throw new GmallException("请先登录！");
        }
        redisTemplate.delete(RedisConst.USER_LOGIN_KEY_PREFIX + token);
    }

    @Override
    public Map<String, Object> login(UserInfo userInfo, HttpServletRequest request) {
        // 获取登录的用户名和密码
        String loginName = userInfo.getLoginName();
        // 加密密码
        String password = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        // 根据用户名和密码查询用户信息
        UserInfo user = userInfoMapper.selectOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getLoginName, loginName).eq(UserInfo::getPasswd, password));
        // 判断用户是否存在
        if (user == null) {
            throw new GmallException("用户名或密码错误，请重新输入！");
        }
        // 登录成功，生成token并返回
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        // 保存token到redis中，value为用户id和用户ip地址
        Long id = user.getId();
        String ipAddress = IpUtil.getIpAddress(request);
        JSONObject redisJson = new JSONObject();
        redisJson.put("userId", id);
        redisJson.put("ipAddress", ipAddress);
        redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX + token, redisJson.toJSONString(), RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
        // 返回token
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", id);
        result.put("nickName", user.getNickName());
        return result;
    }

}
