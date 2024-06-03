package com.atguigu.gmall.gateway.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 */
@Configuration
@EnableCaching
public class RedisConfig {
    // 声明模板
    /*
    ref = 表示引用
    value = 具体的值
    <bean class="org.springframework.data.redis.core.RedisTemplate" >
        <property name="defaultSerializer" ref = "">
    </bean>
     */
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        //  设置redis的连接池工厂。
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //  设置序列化的。
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper objectMapper = new ObjectMapper(); //  创建ObjectMapper 对象
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY); // 设置所有字段可见，包括get和set方法
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL); // 设置序列化时将类信息一起序列化
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper); // 设置 ObjectMapper 对象
        //  将Redis 中 string ，hash 数据类型，自动序列化！
        redisTemplate.setKeySerializer(new StringRedisSerializer()); // 设置key的序列化器
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer); // 设置value的序列化器
        //  设置数据类型是Hash 的 序列化！
        redisTemplate.setHashKeySerializer(new StringRedisSerializer()); // 设置hash key的序列化器
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer); // 设置hash value的序列化器

        redisTemplate.afterPropertiesSet(); //  启动模板
        return redisTemplate;
    }

}
