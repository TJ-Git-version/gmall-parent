package com.atguigu.gmall.payment.constant;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "payment.alipay")
public class AlipayConstant {

    private String app_id; // 应用ID
    private String public_key; // 支付宝公钥
    private String private_key; // 商户私钥
    private String gateway_url; // 支付宝网关
    private String format; // 仅支持json
    private String charset; // 仅支持utf-8
    private String sign_type; // 仅支持RSA2
    private String notify_payment_url; // 异步通知地址，支付成功后通知商户服务器
    private String return_order_url; // 支付成功后的跳转地址
    private String return_payment_url; // 同步通知地址，支付成功后跳转的页面
    private String product_code; // 固定值 "FAST_INSTANT_TRADE_PAY"
}
