package com.atguigu.gmall.common.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * redisson配置信息
 */
@Data
@Configuration
@ConfigurationProperties("spring.redis")
public class RedissonConfig {

    private String host;

    private String addresses;

    private String password;

    private String port;

    // 连接超时时间，单位：毫秒
    private int timeout = 3000;

    // 核心线程数
    private int connectionPoolSize = 64;

    // 最小空闲连接数
    private int connectionMinimumIdleSize=10;

    // 心跳包间隔时间，单位：毫秒
    private int pingConnectionInterval = 60000;

    // Redisson地址前缀
    private static String ADDRESS_PREFIX = "redis://";

    /**
     * 自动装配
     *
     */
    @Bean
    RedissonClient redissonSingle() {
        Config config = new Config();
        if(StringUtils.isEmpty(host)){
            throw new RuntimeException("host is  empty");
        }
        SingleServerConfig serverConfig = config.useSingleServer()
                //redis://127.0.0.1:7181
                .setAddress(ADDRESS_PREFIX + this.host + ":" + port) // redis地址
                .setTimeout(this.timeout) // 连接超时时间
                .setPingConnectionInterval(pingConnectionInterval) // 心跳包间隔时间
                .setConnectionPoolSize(this.connectionPoolSize) // 核心线程数
                .setConnectionMinimumIdleSize(this.connectionMinimumIdleSize) // 最小空闲连接数
                ;
        if(!StringUtils.isEmpty(this.password)) {
            serverConfig.setPassword(this.password);
        }
        // RedissonClient redisson = Redisson.create(config);
        return Redisson.create(config);
    }
}