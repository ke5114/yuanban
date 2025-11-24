package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.dao.OrderMapper;
import org.example.model.Attendant;
import org.example.model.Order;
import org.example.model.User;
import org.example.service.AttendantService;
import org.example.service.OrderService;
import org.example.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final UserService userService;
    private final AttendantService attendantService;
    private final Random random = new Random();

    public OrderServiceImpl(OrderMapper orderMapper, UserService userService, AttendantService attendantService) {
        this.orderMapper = orderMapper;
        this.userService = userService;
        this.attendantService = attendantService;
    }

    // 生成唯一订单编号：ORD+年月日时分秒+3位随机数
    private String generateUniqueOrderNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStr = sdf.format(new Date());
        String randomStr = String.format("%03d", random.nextInt(1000));
        return "ORD" + timeStr + randomStr;
    }

    private void validateOrderAmountFields(Order order) {
        if (order.getConsultationDuration() == null || order.getUnitPrice() == null) {
            throw new IllegalArgumentException("创建订单时，陪诊时长和单价为必填项");
        }
    }

    @Override
    @Transactional
    public Order createOrder(Order order) {
        validateOrderAmountFields(order);
        generateVerificationCodeIfAbsent(order);

        // 1. 自动生成唯一订单号
        String orderNo = generateUniqueOrderNo();
        order.setOrderNo(orderNo);
        log.info("生成订单编号：{}", orderNo);

        // 2. 计算结束时间（预约时间 + 陪诊时长）
        if (order.getAppointmentTime() != null && order.getConsultationDuration() != null) {
            long durationMs = order.getConsultationDuration()
                    .multiply(new BigDecimal(3600000)) // 小时转毫秒
                    .longValue();
            Date endTime = new Date(order.getAppointmentTime().getTime() + durationMs);
            order.setEndTime(endTime);
        }

        // 3. 金额计算
        BigDecimal estimatedAmount = order.getConsultationDuration().multiply(order.getUnitPrice());
        order.setOrderAmount(estimatedAmount);
        BigDecimal prepayRate = new BigDecimal("0.3");
        order.setDepositAmount(estimatedAmount.multiply(prepayRate).setScale(2, RoundingMode.HALF_UP));

        // 4. 插入数据库
        orderMapper.insert(order);
        log.info("创建订单成功，编号: {}", orderNo);
        return order;
    }

    @Override
    @Transactional
    public int createAppointmentOrder(Order order) {
        populatePhoneNumbers(order);
        generateVerificationCodeIfAbsent(order);

        // 1. 自动生成唯一订单号
        String orderNo = generateUniqueOrderNo();
        order.setOrderNo(orderNo);
        log.info("生成预约订单编号：{}", orderNo);

        // 2. 计算结束时间
        if (order.getAppointmentTime() != null && order.getConsultationDuration() != null) {
            long durationMs = order.getConsultationDuration()
                    .multiply(new BigDecimal(3600000))
                    .longValue();
            Date endTime = new Date(order.getAppointmentTime().getTime() + durationMs);
            order.setEndTime(endTime);
        }

        // 3. 金额计算
        if (order.getConsultationDuration() != null && order.getUnitPrice() != null) {
            BigDecimal estimatedAmount = order.getConsultationDuration().multiply(order.getUnitPrice());
            order.setOrderAmount(estimatedAmount);
            BigDecimal prepayRate = new BigDecimal("0.3");
            order.setDepositAmount(estimatedAmount.multiply(prepayRate).setScale(2, RoundingMode.HALF_UP));
        }

        // 4. 插入数据库
        int rows = orderMapper.insertAppointmentOrder(order);
        log.info("预约订单创建{}，编号: {}", rows > 0 ? "成功" : "失败", orderNo);
        return rows;
    }

    private void populatePhoneNumbers(Order order) {
        if (order.getUserId() != null) {
            User user = userService.getUserById(order.getUserId());
            if (user != null) order.setUserPhone(user.getPhone());
        }
        if (order.getAttendantId() != null) {
            Attendant attendant = attendantService.getAttendantById(order.getAttendantId());
            if (attendant != null) order.setAttendantPhone(attendant.getPhone());
        }
    }

    private void generateVerificationCodeIfAbsent(Order order) {
        if (order.getVerificationCode() == null) {
            int code = random.nextInt(9000) + 1000;
            order.setVerificationCode(String.valueOf(code));
        }
    }

    // 其他原有方法（verifyVerificationCode、confirmOrder等）保持不变
    @Override
    public String verifyVerificationCode(Integer orderId, String inputCode) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) return "订单不存在";
        String storedCode = order.getVerificationCode();
        if (storedCode == null || storedCode.isEmpty()) return "该订单未生成验证码";
        return storedCode.equals(inputCode) ? "OK" : "陪诊码错误";
    }

    @Override
    public String confirmOrder(Integer orderId, Integer attendantId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) return "订单不存在";
        if (order.getOrderStatus() != 0) return "订单状态错误，当前无法确认接单";

        Attendant attendant = attendantService.getAttendantById(attendantId);
        if (attendant == null) return "陪诊师不存在";

        generateVerificationCodeIfAbsent(order);
        order.setOrderStatus(1);
        order.setAttendantId(attendantId);

        int rows = orderMapper.updateByPrimaryKeySelective(order);
        return rows > 0 ? "确认接单成功" : "确认接单失败";
    }

    @Override
    public String getVerificationCode(Integer orderId, Integer userId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) return "订单不存在";
        if (!order.getUserId().equals(userId)) return "无权获取该订单的陪诊码";
        if (order.getOrderStatus() != 1) return "陪诊师尚未接单，暂无法获取陪诊码";
        return order.getVerificationCode();
    }

    @Override
    public Order getById(Integer orderId) {
        return null;
    }

    @Override
    public List<Order> findOrdersByUserId(Integer userId) {
        return orderMapper.findOrdersByUserId(userId);
    }

    @Override
    public List<Order> findAllOrders() {
        return orderMapper.findAllOrders();
    }

    @Override
    public boolean deleteOrder(Integer orderId) {
        int rows = orderMapper.deleteOrder(orderId);
        return rows > 0;
    }

    @Override
    public Order getOrderById(Integer id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional
    public int updateOrder(Order order) {
        // 合并选择项和自定义需求
        if (order.getSelectedOptions() != null && !order.getSelectedOptions().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Integer optionInt : order.getSelectedOptions()) {
                String optionDesc = getSpecialRequirementDesc(optionInt);
                if (optionDesc != null) sb.append(optionDesc).append("、");
            }
            if (sb.length() > 0) sb.setLength(sb.length() - 1);
            if (order.getCustomRequirement() != null && !order.getCustomRequirement().trim().isEmpty()) {
                sb.append("；").append(order.getCustomRequirement());
            }
            order.setSpecialRequirements(sb.toString());
        } else {
            order.setSpecialRequirements(order.getCustomRequirement());
        }

        // 金额和结束时间重新计算
        if (order.getConsultationDuration() != null && order.getUnitPrice() != null) {
            BigDecimal estimatedAmount = order.getConsultationDuration().multiply(order.getUnitPrice());
            order.setOrderAmount(estimatedAmount);
            BigDecimal prepayRate = new BigDecimal("0.3");
            order.setDepositAmount(estimatedAmount.multiply(prepayRate).setScale(2, RoundingMode.HALF_UP));
        }
        if (order.getAppointmentTime() != null && order.getConsultationDuration() != null) {
            long durationMs = order.getConsultationDuration()
                    .multiply(new BigDecimal(3600000))
                    .longValue();
            order.setEndTime(new Date(order.getAppointmentTime().getTime() + durationMs));
        }

        return orderMapper.updateByPrimaryKeySelective(order);
    }

    @Override
    public String uploadMedicalRecord(Integer orderId, MultipartFile file) {
        try {
            Order order = orderMapper.selectByPrimaryKey(orderId);
            if (order == null) throw new IllegalArgumentException("订单不存在，ID: " + orderId);

            String fileName = UUID.randomUUID() + "." +
                    file.getOriginalFilename().split("\\.")[1];
            String filePath = "uploads/medical_records/" + fileName;
            File dest = new File(filePath);
            if (!dest.getParentFile().exists()) dest.getParentFile().mkdirs();
            file.transferTo(dest);

            order.setElectronicMedicalRecord(filePath);
            orderMapper.updateByPrimaryKeySelective(order);
            return filePath;
        } catch (IOException e) {
            log.error("电子病历上传失败", e);
            throw new RuntimeException("电子病历上传失败", e);
        }
    }

    @Override
    public int updateOrderEvaluation(Order order) {
        return orderMapper.updateOrderEvaluation(order);
    }

    @Override
    public int updateAttendantStarRating(Order order) {
        return orderMapper.updateAttendantStarRating(order);
    }

    @Override
    public List<Map<String, String>> getAllClinicTypes() {
        List<Integer> clinicTypeInts = orderMapper.getAllClinicTypes();
        List<Integer> uniqueTypeInts = clinicTypeInts.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        return uniqueTypeInts.stream()
                .map(typeInt -> {
                    Map<String, String> typeMap = new HashMap<>();
                    typeMap.put("value", String.valueOf(typeInt));
                    typeMap.put("label", getClinicTypeDesc( Integer.valueOf( typeInt ) ));
                    return typeMap;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Order getOrderById(Order id) {
        return null;
    }

    @Override
    public void updatePaymentStatus(Long orderId, String status) {
        Order order = new Order();
        order.setOrderId(Math.toIntExact(orderId));
        order.setPaymentStatus(Integer.valueOf(status));
        orderMapper.updateByPrimaryKeySelective(order);
    }

    private String getSpecialRequirementDesc(Integer optionInt) {
        return switch (optionInt) {
            case 1 -> "代取药";
            case 2 -> "代取报告";
            case 3 -> "需轮椅协助";
            case 4 -> "复查";
            case 5 -> "药物过敏";
            default -> {
                log.warn("无效的特殊需求int值：{}", optionInt);
                yield null;
            }
        };
    }

    private String getClinicTypeDesc(Integer typeInt) {
        return switch (typeInt) {
            case 1 -> "普通陪诊";
            case 2 -> "术后护理";
            case 3 -> "急诊陪同";
            case 4 -> "上门陪诊";
            default -> {
                log.warn("无效的就诊类型int值：{}", typeInt);
                yield "未知类型";
            }
        };
    }
}