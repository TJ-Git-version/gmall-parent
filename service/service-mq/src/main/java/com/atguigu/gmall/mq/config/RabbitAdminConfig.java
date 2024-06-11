// package com.atguigu.gmall.mq.config;
//
// import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
// import org.springframework.amqp.rabbit.core.RabbitAdmin;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
//
// /**
//  * RabbitAdminConfig：创建RabbitAdmin对象，用于创建队列、绑定交换机和队列
//  */
// @Configuration
// public class RabbitAdminConfig {
//
//     @Value("${spring.rabbitmq.addresses}")
//     private String rabbitmqHost;
//
//     /**
//      * 创建连接工厂
//      * @return
//      */
//     @Bean
//     public CachingConnectionFactory cachingConnectionFactory(){
//         rabbitmqHost = rabbitmqHost.substring(0, rabbitmqHost.lastIndexOf(":"));
//         return new CachingConnectionFactory(rabbitmqHost);
//     }
//
//     @Bean
//     public RabbitAdmin rabbitAdmin(){
//         // 传入连接工厂
//         RabbitAdmin rabbitAdmin = new RabbitAdmin(cachingConnectionFactory());
//         // 启动时自动创建队列，绑定交换机和队列
//         rabbitAdmin.setAutoStartup(true);
//         return rabbitAdmin;
//     }
//
// }
