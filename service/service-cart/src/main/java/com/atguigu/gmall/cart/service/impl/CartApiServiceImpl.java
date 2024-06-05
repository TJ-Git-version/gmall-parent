package com.atguigu.gmall.cart.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.spring.util.ObjectUtils;
import com.atguigu.gmall.cart.service.CartApiService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("all")
public class CartApiServiceImpl implements CartApiService {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取当前用户勾选的购物车列表
     * @param request
     * @return
     */
    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        // 获取购物车列表
        List<CartInfo> cartInfoList = null;
        // 总数量
        Integer totalNum = 0;
        // 总金额
        BigDecimal totalAmount = new BigDecimal("0");
        if (StringUtils.isNotBlank(userId)) {
            // 从redis中获取购物车列表
            BoundHashOperations<String, String, CartInfo> boundHashOps = redisTemplate.boundHashOps(getCartKey(userId));
            if (!Objects.isNull(boundHashOps)) {
                cartInfoList = boundHashOps.values();
                if (CollectionUtils.isNotEmpty(cartInfoList)) {
                    // 过滤掉未勾选的商品
                    cartInfoList = cartInfoList
                            .stream()
                            .filter(cartInfo -> cartInfo.getIsChecked() == 1)
                            .collect(Collectors.toList());
                    }
            }
        }
        return cartInfoList;
    }

    /**
     * 取消勾选购物车中的商品
     * @param skuId
     * @param isChecked
     * @param request
     */
    @Override
    public void checkCart(Long skuId, Integer isChecked, HttpServletRequest request) {
        // 获取登录用户id
        String userId = getUserId(request);
        // 获取购物车key
        String cartKey = getCartKey(userId);
        BoundHashOperations<String, String, CartInfo> boundHashOps = redisTemplate.boundHashOps(cartKey);
        if (boundHashOps.hasKey(skuId.toString())) {
            CartInfo cartInfo = boundHashOps.get(skuId.toString());
            cartInfo.setIsChecked(isChecked);
            cartInfo.setUpdateTime(new Date());
            boundHashOps.put(skuId.toString(), cartInfo);
        }
    }


    /**
     * 删除购物车中的商品
     * @param skuId
     * @param request
     */
    @Override
    public void deleteCart(Long skuId, HttpServletRequest request) {
        String userId = getUserId(request);
        String cartKey = getCartKey(userId);
        // 判断购物车中是否已经存在该商品，如果存在，则删除
        BoundHashOperations<String, String, CartInfo> boundHashOps = redisTemplate.boundHashOps(cartKey);
        // 判断购物车中是否已经存在该商品，如果存在，则删除
        if (boundHashOps.hasKey(skuId.toString())) {
            boundHashOps.delete(skuId.toString());
        }
    }

    /**
     * 优先获取登录用户id，如果不存在，则获取临时用户id
     * @param request
     * @return
     */
    private static String getUserId(HttpServletRequest request) {
        // 获取临时用户id
        String userId = AuthContextHolder.getUserId(request);
        // 获取用户id
        if (StringUtils.isNotBlank(userId)) {
            userId = AuthContextHolder.getUserTempId(request);
        }
        return userId;
    }


    /**
     * 获取购物车列表， 合并登录用户和临时用户购物车商品
     * @param request
     * @return
     */
    @Override
    public List<CartInfo> cartList(HttpServletRequest request) {
        // 获取临时用户id
        String userTempId = AuthContextHolder.getUserTempId(request);
        List<CartInfo> notLoggedCartInfoList = null;
        // 临时用户购物车数据
        if (StringUtils.isNotBlank(userTempId)) {
            notLoggedCartInfoList = redisTemplate.boundHashOps(getCartKey(userTempId)).values();
        }
        // 登录用户购物车数据
        String userId = AuthContextHolder.getUserId(request);
        // 没有登录用户，直接返回临时用户购物车数据
        if (StringUtils.isEmpty(userId)) {
            if (CollectionUtils.isNotEmpty(notLoggedCartInfoList)) {
                notLoggedCartInfoList.sort((o1, o2) -> DateUtil.truncatedCompareTo(o2.getCreateTime(), o1.getCreateTime(), Calendar.SECOND));
            }
            return notLoggedCartInfoList;
        }

        // 存在登录用户，则合并购物车数据
        BoundHashOperations<String, String, CartInfo> boundHashOps = redisTemplate.boundHashOps(getCartKey(userId));
        if (notLoggedCartInfoList != null) {
            notLoggedCartInfoList.forEach(notLoggedCartInfo -> {
                String skuId = notLoggedCartInfo.getSkuId().toString();
                // 判断购物车中是否已经存在该商品，如果存在，则更新数量，如果不存在，则新增
                if (boundHashOps.hasKey(skuId) && boundHashOps.get(skuId) != null) {
                    notLoggedCartInfo.setSkuPrice(productFeignClient.getSkuPrice(Long.valueOf(skuId)));
                    notLoggedCartInfo.setUpdateTime(new Date());
                    notLoggedCartInfo.setSkuNum(notLoggedCartInfo.getSkuNum() + boundHashOps.get(skuId).getSkuNum());
                    notLoggedCartInfo.setIsChecked(1);
                    boundHashOps.put(skuId, notLoggedCartInfo);
                } else {
                    notLoggedCartInfo.setUserId(userId);
                    notLoggedCartInfo.setSkuPrice(productFeignClient.getSkuPrice(Long.valueOf(skuId)));
                    notLoggedCartInfo.setIsChecked(1);
                    notLoggedCartInfo.setCreateTime(new Date());
                    notLoggedCartInfo.setUpdateTime(new Date());
                    boundHashOps.put(skuId, notLoggedCartInfo);
                }
            });
        }
        // 删除临时用户购物车数据
        redisTemplate.delete(getCartKey(userTempId));

        List<CartInfo> cartInfoList = boundHashOps.values();
        // 根据更新时间进行倒叙排序
        if (cartInfoList != null) {
            cartInfoList.sort((o1, o2) -> DateUtil.truncatedCompareTo(o2.getCreateTime(), o1.getCreateTime(), Calendar.SECOND));
        }
        return cartInfoList;
    }


    /**
     * 添加购物车，如果存在，则更新数量，如果不存在，则新增
     *
     * @param skuId
     * @param skuNum
     * @param request
     */
    @Override
    public void addCart(Long skuId, Integer skuNum, HttpServletRequest request) {
        // 获取购物车中的sku商品信息
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        if (skuInfo == null) {
            throw new GmallException("商品不存在，请刷新后重试！");
        }
        // 判断临时id是否存在，如果存在，则添加到redis中
        String userId = AuthContextHolder.getUserTempId(request);
        if (StringUtils.isNotBlank(AuthContextHolder.getUserId(request))) {
            userId = AuthContextHolder.getUserId(request);
        }
        if (userId != null) {
            // 调用redis服务，添加购物车
            String cartKey = getCartKey(userId);
            BoundHashOperations<String, String, CartInfo> cartHashOps = redisTemplate.boundHashOps(cartKey);
            CartInfo cartInfo = null;
            // 判断购物车中是否已经存在该商品，如果存在，则更新数量，如果不存在，则新增
            if (cartHashOps.hasKey(skuId.toString())) {
                // 更新购物车信息
                cartInfo = cartHashOps.get(skuId.toString());
                cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
                cartInfo.setSkuPrice(productFeignClient.getSkuPrice(skuId));
                cartInfo.setUpdateTime(new Date());
            } else {
                // 新增购物车
                cartInfo = new CartInfo();
                cartInfo.setUserId(userId);
                cartInfo.setSkuId(skuId);
                cartInfo.setCartPrice(skuInfo.getPrice());
                cartInfo.setSkuNum(skuNum);
                cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
                cartInfo.setSkuName(skuInfo.getSkuName());
                cartInfo.setIsChecked(1);
                cartInfo.setSkuPrice(skuInfo.getPrice());
                cartInfo.setCreateTime(new Date());
                cartInfo.setUpdateTime(new Date());
            }
            cartHashOps.put(skuId.toString(), cartInfo);
        }
    }


    /**
     * 抽取方法，获取购物车key
     *
     * @param userId
     * @return
     */
    private String getCartKey(String userId) {
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }
}
