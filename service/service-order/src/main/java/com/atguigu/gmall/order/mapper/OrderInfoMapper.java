package com.atguigu.gmall.order.mapper;

import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
    // Page<OrderInfo> selectPageByUserId(Page<OrderInfo> page, String userId);
    Page<OrderInfo> selectOrderInfoByUserId(@Param("page") Page<OrderInfo> page,@Param("userId") String userId);
}
