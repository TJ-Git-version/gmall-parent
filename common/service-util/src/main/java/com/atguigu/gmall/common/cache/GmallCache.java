package com.atguigu.gmall.common.cache;

import com.atguigu.gmall.common.constant.RedisConst;

import java.lang.annotation.*;

/**
 * 使用注解@GmallCache，实现缓存和分布式锁
 * @Target：声明注解的作用范围，可以是类、方法、接口、注解等。
 * @Retention：声明注解的生命周期，可以是SOURCE、CLASS、RUNTIME。
 * @Inherited：声明注解是否可以被子类继承。
 * @Documented：声明注解是否生成Javadoc。
 * @author Jeff River
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface GmallCache {

    /**
     * 缓存key前缀
     */
    String prefix() default "cache:";

    /**
     * 缓存key后缀
     */
    String suffix() default ":info";

    /**
     * 分布式锁key后缀
     */
    // String lockSuffix() default ":lock";
    String lockSuffix() default ":lock";

    /**
     * 数据缓存过期时间，单位：秒
     */
    int expire() default 24 * 60 * 60;

    /**
     * 记录空对象的缓存过期时间，单位：秒
     */
    int emptyExpire() default 10 * 60;

    /**
     * 尝试获取锁的最大等待时间，单位：秒
     */
    int tryLockTimeout() default 60;

    /**
     * 锁的持有时间，单位：秒
     */
    int lockExpire() default 10;


}
