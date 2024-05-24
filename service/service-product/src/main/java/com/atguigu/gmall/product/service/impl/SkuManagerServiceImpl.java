package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.SkuManagerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@SuppressWarnings("all")
public class SkuManagerServiceImpl implements SkuManagerService {

    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;
    @Autowired
    private RedissonClient redissonClient;


    @Override
    public Map<Object, Object> getSkuValueIdsMap(Long spuId) {
        List<Map<Object, Object>> skuValueIdsMap = skuSaleAttrValueMapper.getSkuValueIdsMap(spuId);
        Map<Object, Object> resultMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(skuValueIdsMap)) {
            skuValueIdsMap.forEach(skuValueIdMap -> {
                resultMap.put(skuValueIdMap.get("value_ids"), skuValueIdMap.get("sku_id"));
            });
        }
        return resultMap;
    }

    /**
     * 通过skuId获取sku对应的平台属性
     *
     * @param skuId
     * @return
     */
    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        return baseAttrInfoMapper.getAttrList(skuId);
    }


    /**
     * 根据skuId和spuId查询商品销售属性组合
     *
     * @param skuId
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return spuSaleAttrMapper.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }


    /**
     * 根据skuId查询sku价格
     *
     * @param skuId
     * @return
     */
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (ObjectUtils.isNotNull(skuInfo)) {
            return skuInfo.getPrice();
        }
        return new BigDecimal("0");
    }


    /**
     * 根据skuId查询sku信息
     *
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        // 第一种使用redis实现分布式锁
        // return getSkuInfoRedis(skuId);

        // 第二种：使用redisson分布式锁
        return getSkuInfoRedisson(skuId);
    }

    /**
     * 根据skuId查询sku信息缓存到Redis，并加分布式锁
     * RedisConst
     *
     * @param skuId
     * @return
     */
    // 第二种：使用redisson分布式锁
    private SkuInfo getSkuInfoRedisson(Long skuId) {
        try {
            // 1、构建redis skuinfo key
            String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
            // 2、根据 skukey 查询redis缓存
            SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            // 3、判断缓存数据是否为空
            if (skuInfo != null) {
                // 4、返回缓存数据
                return skuInfo;
            } else {
                // 5、加分布式锁
                String lockKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
                RLock lock = redissonClient.getLock(lockKey);
                // 第一个参数：等待时间  第二个参数：锁持有时间  第三个参数：时间单位
                boolean lockFlag = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                // 获取到锁了，查询数据库并缓存
                if (lockFlag) {
                    try {
                        // 查询sku基本信息
                        SkuInfo skuInfoDB = getSkuInfoDB(skuId);
                        if (skuInfoDB != null) {
                            // 缓存skuInfo
                            redisTemplate.opsForValue().set(skuKey, skuInfoDB, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                            return skuInfoDB;
                        }
                        skuInfoDB = new SkuInfo();
                        // skuInfo为空，也进行缓存，设置过期时间10分钟
                        redisTemplate.opsForValue().set(skuKey, skuInfoDB, RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.MINUTES);
                        return skuInfoDB;
                    } catch (Exception e) {
                        log.error("获取sku信息异常：{}", e.getMessage());
                    } finally {
                        lock.unlock();
                    }
                }else {
                    // 其他线程等待
                    Thread.sleep(1000);
                    return getSkuInfoRedisson(skuId);
                }
            }
        } catch (InterruptedException e) {
            log.error("获取sku信息异常：{}", e.getMessage());
            // throw new GmallException("获取sku信息异常，请联系管理员，谢谢！");
        }
        // 防止缓存宕机，进行兜底方法
        return getSkuInfoDB(skuId);
    }

    // private SkuInfo getSkuInfoRedisson1(Long skuId) {
    //     SkuInfo skuInfo = null;
    //     try {
    //         // 缓存存储数据：key-value
    //         // 定义key sku:skuId:info
    //         String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
    //         // 获取里面的数据？ redis 有五种数据类型 那么我们存储商品详情 使用哪种数据类型？
    //         // 获取缓存数据
    //         skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
    //         // 如果从缓存中获取的数据是空
    //         if (skuInfo == null) {
    //             // 直接获取数据库中的数据，可能会造成缓存击穿。所以在这个位置，应该添加锁。
    //             // 第二种：redisson
    //             // 定义锁的key sku:skuId:lock  set k1 v1 px 10000 nx
    //             String lockKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
    //             RLock lock = redissonClient.getLock(lockKey);
    //         /*
    //         第一种： lock.lock();
    //         第二种:  lock.lock(10,TimeUnit.SECONDS);
    //         第三种： lock.tryLock(100,10,TimeUnit.SECONDS);
    //          */
    //             // 尝试加锁
    //             boolean res = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
    //             if (res) {
    //                 try {
    //                     // 处理业务逻辑 获取数据库中的数据
    //                     // 真正获取数据库中的数据 {数据库中到底有没有这个数据 = 防止缓存穿透}
    //                     skuInfo = getSkuInfoDB(skuId);
    //                     // 从数据库中获取的数据就是空
    //                     if (skuInfo == null) {
    //                         // 为了避免缓存穿透 应该给空的对象放入缓存
    //                         SkuInfo skuInfo1 = new SkuInfo(); // 对象的地址
    //                         redisTemplate.opsForValue().set(skuKey, skuInfo1, RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
    //                         return skuInfo1;
    //                     }
    //                     // 查询数据库的时候，有值
    //                     redisTemplate.opsForValue().set(skuKey, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
    //
    //                     // 使用redis 用的是lua 脚本删除 ，但是现在用么？ lock.unlock
    //                     return skuInfo;
    //
    //                 } catch (Exception e) {
    //                     e.printStackTrace();
    //                 } finally {
    //                     // 解锁：
    //                     lock.unlock();
    //                 }
    //             } else {
    //                 // 其他线程等待
    //                 Thread.sleep(1000);
    //                 return getSkuInfo(skuId);
    //             }
    //         } else {
    //
    //             return skuInfo;
    //         }
    //     } catch (InterruptedException e) {
    //         e.printStackTrace();
    //     }
    //     // 为了防止缓存宕机：从数据库中获取数据
    //     return getSkuInfoDB(skuId);
    // }

    // 第一种：使用redis
    private SkuInfo getSkuInfoRedis(Long skuId) {
        try {
            // 1、构建redis skuinfo key
            String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
            // 2、根据 skukey 查询redis缓存
            SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            // 3、判断缓存数据是否为空
            if (skuInfo != null) {
                // 4、返回缓存数据
                return skuInfo;
            } else {
                // 5、加分布式锁
                String lockKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
                String uuid = UUID.randomUUID().toString().replace("-", "");
                Boolean lockFlag = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                // 获取到锁了，查询数据库并缓存
                if (lockFlag) {
                    // 查询sku基本信息
                    SkuInfo skuInfoDB = getSkuInfoDB(skuId);
                    if (skuInfoDB != null) {
                        // 缓存skuInfo
                        redisTemplate.opsForValue().set(skuKey, skuInfoDB, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                        return skuInfoDB;
                    }
                    skuInfoDB = new SkuInfo();
                    // skuInfo为空，也进行缓存，设置过期时间10分钟
                    redisTemplate.opsForValue().set(skuKey, skuInfoDB, RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.MINUTES);
                    // 释放锁，使用lua脚本
                    String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                            "then \n" +
                            "    return redis.call(\"del\",KEYS[1])\n" +
                            "else \n" +
                            "    return 0\n" +
                            "end";
                    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                    redisScript.setScriptText(script);
                    redisScript.setResultType(Long.class);
                    redisTemplate.execute(redisScript, Arrays.asList(lockKey), uuid);
                    return skuInfoDB;
                } else {
                    // 自旋等待锁释放
                    Thread.sleep(500);
                    getSkuInfoRedis(skuId);
                }
            }
        } catch (Exception e) {
            log.error("获取sku信息异常：{}", e.getMessage());
            // throw new GmallException("获取sku信息异常，请联系管理员，谢谢！");
        }
        // 防止缓存宕机，进行兜底方法
        return getSkuInfoDB(skuId);
    }

    /**
     * 根据skuId查询sku信息（查询数据库）
     *
     * @param skuId
     * @return
     */
    private SkuInfo getSkuInfoDB(Long skuId) {
        // 查询sku基本信息
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (ObjectUtils.isNotNull(skuInfo)) {
            // 查询sku图片信息
            List<SkuImage> skuImageList = skuImageMapper.selectList(Wrappers.<SkuImage>lambdaQuery().eq(SkuImage::getSkuId, skuId));
            skuInfo.setSkuImageList(skuImageList);
        }
        return skuInfo;
    }


    /**
     * 上架/下架sku
     *
     * @param skuId
     * @deprecated 是否销售（1：是 0：否）
     */
    @Override
    public void skuSaleStatusChange(Long skuId, Integer isSale) {
        skuInfoMapper.update(null,
                Wrappers.<SkuInfo>lambdaUpdate()
                        .set(SkuInfo::getIsSale, isSale)
                        .eq(SkuInfo::getId, skuId));
    }


    /**
     * 分页查询sku列表
     *
     * @param page
     * @return
     */
    @Override
    public IPage<SkuInfo> getSkuListPage(Page<SkuInfo> page) {
        LambdaQueryWrapper<SkuInfo> lambdaQueryWrapper = Wrappers.<SkuInfo>lambdaQuery()
                .orderByDesc(SkuInfo::getSpuId)
                .orderByDesc(SkuInfo::getPrice);
        Page<SkuInfo> skuInfoPage = skuInfoMapper.selectPage(page, lambdaQueryWrapper);
        skuInfoPage.getRecords().forEach(skuInfo -> {
            BaseTrademark baseTrademark = baseTrademarkMapper.selectById(skuInfo.getTmId());
            skuInfo.setTmName(baseTrademark == null ? "未知品牌" : baseTrademark.getTmName());
        });
        return skuInfoPage;
    }

    /**
     * 保存sku信息
     *
     * @param skuInfo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuInfo(SkuInfo skuInfo) {
        // 保存skuInfo
        skuInfoMapper.insert(skuInfo);
        // 保存sku图片信息
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (CollectionUtils.isNotEmpty(skuImageList)) {
            skuImageList.forEach(skuImage -> {
                skuImage.setSkuId(skuInfo.getId());
            });
            skuImageMapper.insertBatch(skuImageList);
        }
        // 保存sku平台属性关联信息
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (CollectionUtils.isNotEmpty(skuAttrValueList)) {
            skuAttrValueList.forEach(skuAttrValue -> {
                skuAttrValue.setSkuId(skuInfo.getId());
            });
            skuAttrValueMapper.insertBatch(skuAttrValueList);
        }
        // 保存sku销售属性信息
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (CollectionUtils.isNotEmpty(skuSaleAttrValueList)) {
            skuSaleAttrValueList.forEach(skuSaleAttrValue -> {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
            });
            skuSaleAttrValueMapper.insertBatch(skuSaleAttrValueList);
        }
    }


}
