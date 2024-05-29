package com.atguigu.gmall.common.constant;

/**
 * Redis常量配置类
 * set name admin
 */
public class RedisConst {

    public static final String SKUKEY_PREFIX = "sku:"; // sku:10001:info
    public static final String SKUKEY_SUFFIX = ":info"; // sku:10001:info

    public static final String SKUKEY_PRICE_PREFIX = "skuPrice:"; // sku:10001:info
    //单位：秒
    public static final long SKUKEY_TIMEOUT = 24 * 60 * 60; // 1天
    // 定义变量，记录空对象的缓存过期时间
    public static final long SKUKEY_TEMPORARY_TIMEOUT = 10 * 60; // 10分钟

    //单位：秒 尝试获取锁的最大等待时间
    public static final long SKULOCK_EXPIRE_PX1 = 100; // 100秒
    //单位：秒 锁的持有时间
    public static final long SKULOCK_EXPIRE_PX2 = 10; // 1秒
    public static final String SKULOCK_SUFFIX = ":lock"; // sku:10001:lock

    public static final String USER_KEY_PREFIX = "user:"; // user:10001:info
    public static final String USER_CART_KEY_SUFFIX = ":cart"; // user:10001:cart
    public static final long USER_CART_EXPIRE = 60 * 60 * 24 * 30;  // 30天

    //用户登录
    public static final String USER_LOGIN_KEY_PREFIX = "user:login:"; // user:login:10001
    //    public static final String userinfoKey_suffix = ":info"; // user:login:10001:info
    public static final int USERKEY_TIMEOUT = 60 * 60 * 24 * 7; // 7天

    //秒杀商品前缀
    public static final String SECKILL_GOODS = "seckill:goods"; // seckill:goods:10001
    public static final String SECKILL_ORDERS = "seckill:orders"; // seckill:orders:10001
    public static final String SECKILL_ORDERS_USERS = "seckill:orders:users"; // seckill:orders:users:10001
    public static final String SECKILL_STOCK_PREFIX = "seckill:stock:"; // seckill:stock:10001
    public static final String SECKILL_USER = "seckill:user:"; // seckill:user:10001
    //用户锁定时间 单位：秒
    public static final int SECKILL__TIMEOUT = 60 * 60 * 1; // 1小时

    // sku 布隆过滤器key
    public static final String SKU_BLOOM_FILTER = "sku:bloom:filter"; // sku:bloomfilter


}
