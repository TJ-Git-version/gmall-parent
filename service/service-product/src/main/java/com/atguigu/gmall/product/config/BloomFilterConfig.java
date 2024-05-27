package com.atguigu.gmall.product.config;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.product.service.SkuManagerService;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@SuppressWarnings("all")
public class BloomFilterConfig implements CommandLineRunner {

    @Autowired
    private RedissonClient redissonClient;
    
    @Autowired
    private SkuManagerService managerService;

    @Override
    public void run(String... args) throws Exception {
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        bloomFilter.tryInit(1000000, 0.03);
        // 初始化布隆过滤器元素
        skuIdInit(bloomFilter);
        log.info("初始化布隆过滤器完成");
    }

    /**
     * 初始化skuId到布隆过滤器中
     * @param bloomFilter
     */
    private void skuIdInit(RBloomFilter<Long> bloomFilter) {
        // 从数据库中获取所有skuId
        List<Long> skuInfoIds = managerService.getSkuInfoIds();
        if (CollectionUtils.isNotEmpty(skuInfoIds)) {
            skuInfoIds.forEach(skuInfoId ->{
                if (!bloomFilter.contains(skuInfoId)) {
                    bloomFilter.add(skuInfoId);
                }
            });
        }
        log.info("skuId初始化完成，共{}个skuId", skuInfoIds.size());
    }
}
