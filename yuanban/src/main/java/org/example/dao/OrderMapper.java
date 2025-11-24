package org.example.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.model.Order;

import java.util.List;

@Mapper
public interface OrderMapper {
    // 插入订单
    int insert(Order order);

    // 根据用户ID查询订单
    List<Order> findOrdersByUserId(Integer userId);

    // 查询所有订单
    List<Order> findAllOrders();

    // 删除订单
    int deleteOrder(Integer orderId);

    // 插入预约订单
    int insertAppointmentOrder(Order order);

    // 更新订单评价
    int updateOrderEvaluation(Order order);

    // 更新陪诊师星级
    int updateAttendantStarRating(Order order);

    // 获取所有就诊类型
    List<Integer> getAllClinicTypes();

    // 选择性更新订单
    int updateByPrimaryKeySelective(Order order);

    // 根据ID查询订单
    Order selectByPrimaryKey(Integer id);





}