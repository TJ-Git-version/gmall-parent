package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import springfox.documentation.spring.web.json.Json;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
@SuppressWarnings("all")
public class GmallCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    private String prefix;
    private String suffix;
    private String lockSuffix;
    private int expire;
    private int emptyExpire;
    private int tryLockTimeout;
    private int lockExpire;

    /**
     * 初始化缓存参数
     * @param methodSignature
     */

    private void initParamters(MethodSignature methodSignature) {
        GmallCache gmallCache = methodSignature.getMethod().getAnnotation(GmallCache.class);
        // 获取缓存前缀
        prefix = gmallCache.prefix();
        // 获取缓存后缀
        suffix = gmallCache.suffix();
        // 获取缓存锁后缀
        lockSuffix = gmallCache.lockSuffix();
        // 缓存过期时间
        expire = gmallCache.expire();
        // 缓存空值过期时间
        emptyExpire = gmallCache.emptyExpire();
        // 尝试加锁超时时间
        tryLockTimeout = gmallCache.tryLockTimeout();
        // 锁过期时间
        lockExpire = gmallCache.lockExpire();
    }

    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object gmallCacheProfiling(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = new Object();
        // 获取缓存注解
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        // 初始化缓存参数
        initParamters(methodSignature);
        // 获取方法参数
        Object[] args = joinPoint.getArgs();
        try {
            // 构建缓存key
            String infoKey = prefix + Arrays.asList(args) + suffix;
            // 获取缓存数据
            result = getRedisData(infoKey, methodSignature);
            if (result != null) {
                return result;
            } else { // 缓存为空，查询数据库获取数据, 并添加分布式锁
                // 构建分布式锁key
                String lockKey = prefix + Arrays.asList(args) + lockSuffix;
                // 尝试加锁
                RLock lock = redissonClient.getLock(lockKey);
                try {
                    return useRedissionLock(joinPoint, args, infoKey, lock);
                } catch (Throwable e) {
                    log.error("分布式锁异常", e);
                } finally {
                    // 释放锁
                    lock.unlock();
                }
            }
        } catch (Exception e) {
            log.error("缓存异常", e);
        }
        return joinPoint.proceed(args);
    }


    /**
     * 使用Redission分布式锁
     *
     * @param joinPoint
     * @param expire
     * @param args
     * @param infoKey
     * @param lock
     * @return
     * @throws Throwable
     */
    private Object useRedissionLock(ProceedingJoinPoint joinPoint, Object[] args, String infoKey, RLock lock) throws Throwable {
        Object result;
        boolean flag = lock.tryLock(tryLockTimeout, lockExpire, TimeUnit.SECONDS);
        if (flag) {
            // 查询数据库获取数据
            result = joinPoint.proceed(args);
            if (result != null) {
                redisTemplate.opsForValue().set(infoKey, result, expire, TimeUnit.SECONDS);
                log.info("缓存数据成功, key: " + infoKey);
                return result;
            } else {
                // 缓存为空，设置空值
                MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
                Class clazz = methodSignature.getReturnType();
                result = clazz.getConstructor().newInstance();
                redisTemplate.opsForValue().set(infoKey, JSON.toJSONString(result), emptyExpire, TimeUnit.SECONDS);
                log.info("缓存数据为空, 设置空值, key: " + infoKey);
                return result;
            }
        } else {
            Thread.sleep(500);
            return gmallCacheProfiling(joinPoint);
        }
    }

    /**
     * 获取缓存数据
     *
     * @param infoKey
     * @param signature
     * @return
     */
    private Object getRedisData(String infoKey, MethodSignature signature) {
        // 获取缓存数据
        String redisData = JSON.toJSONString(redisTemplate.opsForValue().get(infoKey));
        // 判断缓存是否为空
        if (StringUtils.isNotBlank(redisData)) {
            // 反序列化
            Class clazz = signature.getReturnType();
            return JSON.parseObject(redisData, clazz);
        }
        return null;
    }

}
