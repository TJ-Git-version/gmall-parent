package com.atguigu.gmall.order.service.impl;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.PaymentWay;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderManagerService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.rabbit.constant.MqConst;
import com.atguigu.gmall.rabbit.service.RabbitService;
import com.atguigu.gmall.user.client.UserFeignClient;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@Slf4j
@SuppressWarnings("all")
public class OrderManagerServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderManagerService {

    @Autowired
    private CartFeignClient cartFeignClient;
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private RabbitService rabbitService;

    /**
     * 修改订单状态：关闭状态
     */
    @Override
    public void cancelOrderStatus(OrderInfo orderInfo) {
        updateOrderStatus(orderInfo.getId(), ProcessStatus.CLOSED);
    }

    /**
     * 根据订单id和订单状态更新订单状态
     */
    @Override
    public void updateOrderStatus(Long orderId, ProcessStatus processStatus) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        // 订单状态
        orderInfo.setOrderStatus(processStatus.getOrderStatus().name());
        // 流程状态
        orderInfo.setProcessStatus(processStatus.name());
        baseMapper.updateById(orderInfo);
    }


    /**
     * 根据用户id获取订单列表
     */
    @Override
    public IPage<OrderInfo> getMyOrderByUserId(Page<OrderInfo> page, String userId) {
        // page = orderInfoMapper.selectPageByUserId(page, userId);
        page = orderInfoMapper.selectOrderInfoByUserId(page, userId);
        page.getRecords().forEach(orderInfo -> {
            orderInfo.setOrderStatusName(OrderStatus.getStatusNameByStatus(orderInfo.getOrderStatus()));
        });
        return page;
    }


    /**
     * 校验商品库存是否充足和价格是否正确
     *
     * @param orderDetailList
     * @return
     */
    @Override
    public List<String> checkSkuStockAndPrice(List<OrderDetail> orderDetailList) {
        if (CollectionUtils.isEmpty(orderDetailList)) {
            return Collections.emptyList();
        }
        AtomicBoolean flag = new AtomicBoolean(true);
        // 异步校验库存和价格
        List<CompletableFuture> futureList = new ArrayList<>();
        // 收集异常信息
        List<String> errorList = new ArrayList<>();
        orderDetailList.forEach(orderDetail -> {
            // TODO 校验库存是否充足
            CompletableFuture<Void> stockCompletableFuture = CompletableFuture.runAsync(() -> {
                boolean hasStock = this.checkSkuStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
                if (!hasStock) {
                    errorList.add("商品" + orderDetail.getSkuName() + "库存不足");
                }
            }, threadPoolExecutor);
            futureList.add(stockCompletableFuture);
            // TODO 校验价格是否变动
            CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
                boolean priceChanged = this.checkSkuPrice(orderDetail.getSkuId(), orderDetail.getOrderPrice());
                if (!priceChanged) {
                    errorList.add("商品" + orderDetail.getSkuName() + "价格已变动");
                }
            }, threadPoolExecutor);
            futureList.add(priceCompletableFuture);
            // TODO 校验优惠券是否可用
        });
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).join();
        return errorList;
    }

    @Override
    public void updateCartCache(String userId) {
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);
        if (CollectionUtils.isNotEmpty(cartCheckedList)) {
            BoundHashOperations<String, String, CartInfo> boundHashOps = redisTemplate.boundHashOps(getCartKey(userId));
            if (Objects.nonNull(boundHashOps)) {
                cartCheckedList.forEach(cartInfo -> {
                    boundHashOps.put(cartInfo.getSkuId().toString(), cartInfo);
                });
            }
        }
    }


    /**
     * 获取缓存购物车key
     *
     * @param userId
     * @return
     */
    private String getCartKey(String userId) {
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }

    /**
     * 校验价格是否变动
     *
     * @param skuId
     * @param orderPrice
     * @return
     */
    private boolean checkSkuPrice(Long skuId, BigDecimal orderPrice) {
        BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
        return Objects.nonNull(skuPrice) && skuPrice.compareTo(orderPrice) == 0;
    }

    @Value("${ware.url}")
    private String wareUrl;

    /**
     * 校验商品库存是否充足
     *
     * @param skuId
     * @param skuNum
     * @return
     */
    private boolean checkSkuStock(Long skuId, Integer skuNum) {
        // 构建请求参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("skuId", skuId);
        paramMap.put("num", skuNum);
        // 链式构建请求
        String stockResult = HttpRequest.get(wareUrl + "hasStock")
                .header(Header.USER_AGENT, "GMALL-PROXY")// 头信息，多个头信息多次调用此方法即可
                .form(paramMap)// 表单内容
                .timeout(20000)// 超时，毫秒
                .execute().body();
        log.info("库存校验结果：{}", stockResult);
        return "1".equals(stockResult);
    }

    public static void main(String[] args) {
        // 构建请求参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("skuId", 21);
        paramMap.put("num", 1);
        String result2 = HttpRequest.get("http://localhost:9001/hasStock")
                .header(Header.USER_AGENT, "GMALL-PROXY")// 头信息，多个头信息多次调用此方法即可
                .form(paramMap)// 表单内容
                .timeout(20000)// 超时，毫秒
                .execute().body();
        System.out.println(result2);
    }

    /**
     * 校验订单交易流水号
     *
     * @param tradeNo
     * @return
     */
    @Override
    public Boolean checkTradeNo(String tradeNo) {
        String redisTradeNo = (String) redisTemplate.opsForValue().get(getTradeNoKey(tradeNo));
        if (StringUtils.isBlank(redisTradeNo)) {
            return false;
        }
        return tradeNo.equals(redisTradeNo);
    }

    /**
     * 删除订单交易流水号
     *
     * @param tradeNo
     */
    @Override
    public void deleteTradeNo(String tradeNo) {
        redisTemplate.delete(getTradeNoKey(tradeNo));
    }


    /**
     * 提交订单
     *
     * @param orderInfo
     * @param tradeNo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitOrder(OrderInfo orderInfo, String tradeNo) {
        // 1. 保存订单信息
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        // 计算总金额，必须要存在订单明细信息
        if (CollectionUtils.isNotEmpty(orderDetailList)) {
            orderInfo.sumTotalAmount();
        }
        // 订单状态，默认为未支付
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        // 订单进度状态：未支付
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        // 支付方式，默认为线上支付、货到付款
        orderInfo.setPaymentWay(PaymentWay.ONLINE.name());
        // 订单交易编号，第三方支付系统生成的唯一订单号
        orderInfo.setOutTradeNo(getOutTradeNo());
        // 订单描述，第三方支付使用
        orderInfo.setTradeBody(getTradeBody(orderDetailList));
        // 新增订单时间
        orderInfo.setOperateTime(new Date());
        // 过期时间，默认30分钟
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 30);
        orderInfo.setExpireTime(calendar.getTime());
        // 保存订单信息
        orderInfoMapper.insert(orderInfo);
        // 2. 保存订单明细信息
        if (CollectionUtils.isNotEmpty(orderDetailList)) {
            orderDetailList.forEach(orderDetail -> {
                orderDetail.setOrderId(orderInfo.getId());
                orderDetailMapper.insert(orderDetail);
            });
        }
        // 提交订单成功后，删除订单流水号
        this.deleteTradeNo(tradeNo);

        // 将订单发送到rabbbitmq，半小时后如果未支付则修改订单状态为关闭状态
        rabbitService.sendDelayMsg(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL, MqConst.ROUTING_ORDER_CANCEL, orderInfo.getId(), MqConst.DELAY_TIME);

        // 删除redis中购物车中已结算的商品
        // String cartKey = RedisConst.USER_KEY_PREFIX + orderInfo.getUserId()+ RedisConst.USER_CART_KEY_SUFFIX;
        // redisTemplate.delete(cartKey);
        return orderInfo.getId();
    }


    /**
     * 获取订单描述信息
     *
     * @param orderDetailList
     * @return
     */
    private String getTradeBody(List<OrderDetail> orderDetailList) {
        String tradeBody = "";
        if (CollectionUtils.isNotEmpty(orderDetailList)) {
            tradeBody = orderDetailList.stream().map(orderDetail -> orderDetail.getSkuName()).collect(Collectors.joining(" "));
            if (tradeBody.length() > 100) {
                tradeBody = tradeBody.toString().substring(0, 100);
            }
        }
        return tradeBody;
    }

    /**
     * 获取订单交易流水号，采用UUID+时间戳的方式生成
     */
    private String getOutTradeNo() {
        StringBuilder outTradeNo = new StringBuilder();
        outTradeNo.append("gmall".toUpperCase())
                .append(UUID.randomUUID().toString().replace("-", ""))
                .append(System.currentTimeMillis());
        return outTradeNo.toString();
    }

    /**
     * 根据用户ID获取订单信息
     *
     * @param userId
     * @return
     */
    @Override
    public Map<String, Object> getTradeInfoByUserId(String userId) {
        // 1. 获取用户地址信息
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(userId);
        // 2. 获取购物车中选中的商品信息
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);
        // 转为订单详情信息
        List<OrderDetail> orderDetailList = null;
        int totalNum = 0;
        BigDecimal totalAmount = new BigDecimal("0");
        if (CollectionUtils.isNotEmpty(cartCheckedList)) {
            orderDetailList = cartCheckedList.stream()
                    .filter(cartInfo -> cartInfo.getIsChecked() == 1)
                    .map(cartInfo -> {
                        // 创建订单明细
                        OrderDetail orderDetail = new OrderDetail();
                        orderDetail.setSkuId(cartInfo.getSkuId());
                        orderDetail.setSkuName(cartInfo.getSkuName());
                        orderDetail.setSkuNum(cartInfo.getSkuNum());
                        orderDetail.setImgUrl(cartInfo.getImgUrl());
                        orderDetail.setOrderPrice(productFeignClient.getSkuPrice(cartInfo.getSkuId()));
                        return orderDetail;
                    }).collect(Collectors.toList());
            // 计算总数量
            totalNum = orderDetailList.stream().mapToInt(OrderDetail::getSkuNum).sum();
            // 计算总金额
            // 第一个参数：初始值
            // 第二个参数：BinaryOperator，用于合并两个元素的函数，其中第一个参数是已经计算出的结果，第二个参数是当前元素
            // 第三个参数：指定方法，用于对流中的数据相加
            // totalAmount = orderDetailList.stream().reduce(totalAmount, (result, orderDetail) -> result.add(orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum()))), BigDecimal::add);
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setOrderDetailList(orderDetailList);
            orderInfo.sumTotalAmount();
            totalAmount = orderInfo.getTotalAmount();
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("userAddressList", userAddressList);
        resultMap.put("detailArrayList", orderDetailList);
        resultMap.put("totalNum", totalNum);
        resultMap.put("totalAmount", totalAmount);
        resultMap.put("tradeNo", getTradeNo(userId));
        return resultMap;
    }

    /**
     * 解决订单重复提交，采用UUID+时间戳的方式生成流水号
     * 获取订单号，采用UUID+时间戳的方式生成
     *
     * @return
     */
    private String getTradeNo(String userId) {
        // 生成订单流水号
        StringBuilder tradeNo = new StringBuilder();
        tradeNo.append(UUID.randomUUID().toString().replace("-", ""))
                .append(System.currentTimeMillis());
        // 存储到redis中
        redisTemplate.opsForValue().set(getTradeNoKey(tradeNo.toString()), tradeNo.toString(), 30, TimeUnit.MINUTES);
        return tradeNo.toString();
    }

    private String getTradeNoKey(String tradeNo) {
        return RedisConst.USER_KEY_PREFIX + RedisConst.ORDER_NO_SUFFIX + tradeNo.toString();
    }

}
