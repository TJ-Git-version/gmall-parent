package com.atguigu.gmall.payment.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.atguigu.gmall.payment.constant.AlipayConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AlipayConstant.class)
@SuppressWarnings("all")
public class AlipayConfig {

    @Autowired
    private AlipayConstant alipayConstant;

    @Bean
    public AlipayClient alipayClient() throws AlipayApiException {
        return new DefaultAlipayClient(getAlipayConfig());
    }

    private com.alipay.api.AlipayConfig getAlipayConfig() {
        com.alipay.api.AlipayConfig alipayConfig = new com.alipay.api.AlipayConfig();
        alipayConfig.setServerUrl(alipayConstant.getGateway_url());
        alipayConfig.setAppId(alipayConstant.getApp_id());
        alipayConfig.setPrivateKey(alipayConstant.getPrivate_key());
        alipayConfig.setFormat(alipayConstant.getFormat());
        alipayConfig.setAlipayPublicKey(alipayConstant.getPublic_key());
        alipayConfig.setCharset(alipayConstant.getCharset());
        alipayConfig.setSignType(alipayConstant.getSign_type());
        return alipayConfig;
    }
}
