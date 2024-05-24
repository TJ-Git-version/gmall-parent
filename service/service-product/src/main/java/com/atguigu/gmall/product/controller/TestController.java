package com.atguigu.gmall.product.controller;


import com.atguigu.gmall.common.result.Result;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/admin/product/test")
public class TestController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @GetMapping("/read")
    public Result read() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("readWriteLock");
        RLock rLock = readWriteLock.readLock();
        rLock.lock(10, TimeUnit.SECONDS);
        String msg = stringRedisTemplate.opsForValue().get("msg");
        // rLock.unlock();
        return Result.ok(msg);
    }


    @GetMapping("/write")
    public Result write() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("readWriteLock");
        RLock writeLock = readWriteLock.writeLock();
        writeLock.lock(10, TimeUnit.SECONDS);
        stringRedisTemplate.opsForValue().set("msg", String.valueOf(System.currentTimeMillis()));
        // rLock.unlock();
        return Result.ok();
    }



    /**
     * 实现分布式锁
     * 1. 先获取锁
     * 2. 如果获取锁成功，则执行业务逻辑，并释放锁
     * 3. 如果获取锁失败，则休眠100ms，再次尝试获取锁
     * 4. 直到获取锁成功为止
     * 5. 注意：如果业务逻辑执行时间过长，锁过期时间应该设置短一些，防止死锁发生
     * 如何设置过期时间过短，导致线程A释放了锁，后面线程B获取到锁，又执行了业务，
     * 但此时线程A执行完了业务，释放了线程B的锁，导致线程C又获取到了锁，以此循环，导致分布式锁失效。
     * 这种现象是因为线程释放了其他线程的锁？
     * 解决方案：给每个线程的锁加唯一标识，避免不同线程释放同一个锁。
     *    使用uuid作为锁的标识，作为锁的值
     * @return
     */
    @GetMapping("/testLock")
    public Result testLock(){
        String key = "sku:"+1314+":info";
        RLock lock  = redissonClient.getLock(key);
        try {
            // lock.lock();
            // lock.lock(30, TimeUnit.SECONDS); // 设置锁的过期时间
            lock.tryLock(100, 30, TimeUnit.SECONDS); // 尝试获取锁，最多等待100ms，最多尝试30次
            Integer num = Integer.valueOf(stringRedisTemplate.opsForValue().get("num"));
            num++;
            stringRedisTemplate.opsForValue().set("num", String.valueOf(num));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
        return Result.ok();
    }

    // @GetMapping("/testLock")
    // public Result testLock(){
    //     String uuid = UUID.randomUUID().toString().replaceAll("-", "");
    //     Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 5, TimeUnit.SECONDS);
    //     if (lock) {
    //         Integer num = Integer.valueOf(stringRedisTemplate.opsForValue().get("num"));
    //         num++;
    //         stringRedisTemplate.opsForValue().set("num", String.valueOf(num));
    //
    //         // 使用lua脚本实现原子性操作
    //         String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
    //                 "then \n" +
    //                 "    return redis.call(\"del\", KEYS[1])\n" +
    //                 "else \n" +
    //                 "    return 0\n" +
    //                 "end";
    //         // 创建脚本对象
    //         DefaultRedisScript<Long> redisScript = new DefaultRedisScript();
    //         // 封装脚本
    //         redisScript.setScriptText(script);
    //         redisScript.setResultType(Long.class);
    //         stringRedisTemplate.execute(redisScript, Arrays.asList("lock"), uuid);
    //         // 释放锁 判断和删除锁的不是原子性
    //         // if (uuid.equals(stringRedisTemplate.opsForValue().get("lock"))) {
    //         //     stringRedisTemplate.delete("lock");
    //         // }
    //     } else {
    //         try {
    //             Thread.sleep(100);
    //         } catch (InterruptedException e) {
    //             throw new RuntimeException(e);
    //         }
    //         testLock();
    //     }
    //
    //     return Result.ok();
    // }

    // 本地锁的演示
    /*
    @GetMapping("/testLock")
    public Result testLock(){
        synchronized (TestController.class) {
            Integer num = Integer.valueOf(stringRedisTemplate.opsForValue().get("num"));
            num++;
            stringRedisTemplate.opsForValue().set("num", String.valueOf(num));
        }
        return Result.ok();
    }
     */

}
