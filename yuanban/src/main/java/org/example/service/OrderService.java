package org.example.service;

import org.example.model.Order;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface OrderService {
    List<Order> findOrdersByUserId(Integer userId);
    List<Order> findAllOrders();
    boolean deleteOrder(Integer orderId);
    Order createOrder(Order order);
    int createAppointmentOrder(Order order);
    int updateOrderEvaluation(Order order);
    int updateAttendantStarRating(Order order);
    List<Map<String, String>> getAllClinicTypes();
    Order getOrderById(Order id);

    Order getOrderById(Integer id);

    int updateOrder(Order order);
    String uploadMedicalRecord(Integer orderId, MultipartFile file);
    void updatePaymentStatus(Long orderId, String status);

    /*--新增验证码校验方法-*/
    String verifyVerificationCode(Integer orderId, String inputCode);
    // 陪诊师确认接单（生成陪诊码）
    String confirmOrder(Integer orderId, Integer attendantId);
    // 用户获取陪诊码（仅在已接单状态下返回）
    String getVerificationCode(Integer orderId, Integer userId);

    Order getById(Integer orderId);
}