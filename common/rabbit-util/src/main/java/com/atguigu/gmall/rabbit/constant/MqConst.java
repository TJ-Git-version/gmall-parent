package com.atguigu.gmall.rabbit.constant;

public class MqConst {
    /**
     * 消息补偿
     */
    public static final String MQ_KEY_PREFIX = "mq:list";
    public static final int RETRY_COUNT = 3;
    /**
     * 商品上下架
     */
    public static final String EXCHANGE_DIRECT_GOODS = "exchange.direct.goods"; // 交换机
    public static final String ROUTING_GOODS_UPPER = "goods.upper"; // 上架路由key
    public static final String ROUTING_GOODS_LOWER = "goods.lower"; // 下架路由key
    //队列
    public static final String QUEUE_GOODS_UPPER  = "queue.goods.upper"; // 上架队列
    public static final String QUEUE_GOODS_LOWER  = "queue.goods.lower"; // 下架队列

    /**
     * 商品上下架：死讯队列
     */
    // public static final String DEAD_EXCHANGE_DIRECT_GOODS = "dead.exchange.direct.goods"; // 交换机
    // public static final String DEAD_ROUTING_GOODS_UPPER = "dead.goods.upper"; // 上架路由key
    // public static final String DEAD_ROUTING_GOODS_LOWER = "dead.goods.lower"; // 下架路由key
    // //队列
    // public static final String DEAD_QUEUE_GOODS_UPPER  = "dead.queue.goods.upper"; // 上架队列
    // public static final String DEAD_QUEUE_GOODS_LOWER  = "dead.queue.goods.lower"; // 下架队列

    /**
     * 取消订单，发送延迟队列
     */
    public static final String EXCHANGE_DIRECT_ORDER_CANCEL = "exchange.direct.order.cancel";//"exchange.direct.order.create" test_exchange;
    public static final String ROUTING_ORDER_CANCEL = "order.create";
    //延迟取消订单队列
    public static final String QUEUE_ORDER_CANCEL  = "queue.order.cancel";
    //取消订单 延迟时间 单位：秒 真实业务
    public static final int DELAY_TIME  = 24*60*60;
    //  测试取消订单
    // public static final int DELAY_TIME  = 3;
    /**
     * 订单支付
     */
    public static final String EXCHANGE_DIRECT_PAYMENT_PAY = "exchange.direct.payment.pay";
    public static final String ROUTING_PAYMENT_PAY = "payment.pay";
    //队列
    public static final String QUEUE_PAYMENT_PAY  = "queue.payment.pay";

    /**
     * 减库存
     */
    public static final String EXCHANGE_DIRECT_WARE_STOCK = "exchange.direct.ware.stock";
    public static final String ROUTING_WARE_STOCK = "ware.stock";
    //队列
    public static final String QUEUE_WARE_STOCK  = "queue.ware.stock";
    /**
     * 减库存成功，更新订单状态
     */
    public static final String EXCHANGE_DIRECT_WARE_ORDER = "exchange.direct.ware.order";
    public static final String ROUTING_WARE_ORDER = "ware.order";
    //队列
    public static final String QUEUE_WARE_ORDER  = "queue.ware.order";

    /**
     * 关闭交易
     */
    public static final String EXCHANGE_DIRECT_PAYMENT_CLOSE = "exchange.direct.payment.close";
    public static final String ROUTING_PAYMENT_CLOSE = "payment.close";
    //队列
    public static final String QUEUE_PAYMENT_CLOSE  = "queue.payment.close";
    /**
     * 定时任务
     */
    public static final String EXCHANGE_DIRECT_TASK = "exchange.direct.task";
    public static final String ROUTING_TASK_1 = "seckill.task.1";
    //队列
    public static final String QUEUE_TASK_1  = "queue.task.1";
    /**
     * 秒杀
     */
    public static final String EXCHANGE_DIRECT_SECKILL_USER = "exchange.direct.seckill.user";
    public static final String ROUTING_SECKILL_USER = "seckill.user";
    //队列
    public static final String QUEUE_SECKILL_USER  = "queue.seckill.user";

    /**
     * 定时任务
     */

    public static final String ROUTING_TASK_18 = "seckill.task.18";
    //队列
    public static final String QUEUE_TASK_18  = "queue.task.18";


}
