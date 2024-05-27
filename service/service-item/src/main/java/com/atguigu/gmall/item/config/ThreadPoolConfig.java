package com.atguigu.gmall.item.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {

    /**
     * 线程池参数：
     * corePoolSize：核心线程数
     * maximumPoolSize：最大线程数
     * keepAliveTime：线程空闲时间
     * unit：时间单位
     * workQueue：任务队列
     *          这里使用了ArrayBlockingQueue作为任务队列，其容量为10000，可以存储10000个任务。
     * threadFactory：线程工厂，用于创建线程
     * handler：线程池拒绝策略，当线程池队列已满，且线程数目达到最大线程数时，如何处理新任务。
     *          这里使用了CallerRunsPolicy策略，即当线程池队列已满，则由调用者所在的线程来运行任务。
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(
                50,
                500,
                30,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10000)
        );
    }

}
