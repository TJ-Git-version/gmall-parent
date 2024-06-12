package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface OrderManagerService extends IService<OrderInfo> {

    /**
     * 根据用户id查询交易信息
     *
     * @param userId
     * @return
     */
    Map<String, Object> getTradeInfoByUserId(String userId);

    /**
     * 提交订单
     *
     * @param orderInfo
     * @param tradeNo
     * @return
     */
    Long submitOrder(OrderInfo orderInfo, String tradeNo);

    /**
     * 校验订单流水号
     *
     * @param tradeNo
     * @return
     */
    Boolean checkTradeNo(String tradeNo);

    /**
     * 根据交易号删除订单流水号
     *
     * @param tradeNo
     */
    void deleteTradeNo(String tradeNo);

    /**
     * 校验商品库存是否充足和价格是否正确
     *
     * @param orderDetailList
     * @return
     */
    List<String> checkSkuStockAndPrice(List<OrderDetail> orderDetailList);

    /**
     * 更新购物车缓存
     *
     * @param userId
     */
    void updateCartCache(String userId);

    /**
     * 根据用户id查询订单列表
     *
     * @param page
     * @param userId
     * @return
     */
    IPage<OrderInfo> getMyOrderByUserId(@Param("page") Page<OrderInfo> page, @Param("userId") String userId);

    /**
     * 修改订单状态：关闭状态
     *
     * @param orderInfo
     */
    void cancelOrderStatus(OrderInfo orderInfo);

    /**
     * 根据订单id和状态名称更新订单状态
     *
     * @param id
     */
    void updateOrderStatus(Long id, ProcessStatus processStatus);

}
