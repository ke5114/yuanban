package org.example.service;

import org.example.model.Attendant;
import org.example.model.Order;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AttendantService {
    // 注册陪诊师
    int register(Attendant attendant);

    @Transactional
    int register(Attendant attendant, Integer userId);

    // 陪诊师登录
    Attendant login(String username, String password);

    Order createOrder(Order order);

    // 支付成功后更新状态
    void updatePaymentStatus(Long orderId, String status);

    // 根据用户名查询陪诊师
    Attendant findByUsername(String username);

    // 根据ID查询陪诊师
    Attendant findById(Integer id);

    // 查询所有陪诊师
    List<Attendant> findAll();

    // 更新陪诊师信息
    int update(Attendant attendant);

    // 删除陪诊师
    int delete(Integer id);

    // 检查用户名是否存在
    boolean existsByUsername(String username);

    Attendant getAttendantById(Integer attendantId);

    boolean existsByUserId(Long userId);
}