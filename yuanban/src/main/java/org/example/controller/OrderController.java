package org.example.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.example.model.Order;
import org.example.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/orders")
@Api(tags = "订单管理接口", description = "包含订单创建、查询、删除、评价等操作")
public class OrderController {

    @Autowired
    private OrderService orderService;
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @GetMapping("/user/{userId}")
    @ApiOperation(value = "查询用户的所有订单", notes = "通过用户ID查询该用户的所有订单")
    @ApiImplicitParam(
            name = "userId",
            value = "用户ID",
            required = true,
            dataType = "int",
            paramType = "path",
            example = "1001"
    )
    public List<Order> findOrdersByUserId(@PathVariable Integer userId) {
        logger.info("查询用户 {} 的订单", userId);
        return orderService.findOrdersByUserId(userId);
    }

    @GetMapping
    @ApiOperation(value = "查询所有订单", notes = "返回系统中所有订单的列表")
    public List<Order> findAllOrders() {
        logger.info("查询所有订单");
        return orderService.findAllOrders();
    }

    @DeleteMapping("/{orderId}")
    @ApiOperation(value = "删除订单", notes = "根据订单ID删除指定订单")
    @ApiImplicitParam(
            name = "orderId",
            value = "订单ID",
            required = true,
            dataType = "int",
            paramType = "path",
            example = "1001"
    )
    public String deleteOrder(@PathVariable Integer orderId) {
        logger.info("删除订单 {}", orderId);
        boolean result = orderService.deleteOrder(orderId);
        return result ? "删除成功" : "删除失败";
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "创建订单", notes = "创建新订单，支持高频需求选择 + 自定义补充")
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        try {
            // 合并选择项和自定义需求：枚举getByCode → int值转描述
            if (order.getSelectedOptions() != null && !order.getSelectedOptions().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (Integer optionInt : order.getSelectedOptions()) { // 原String → Integer
                    // 根据int值获取对应描述（与实体类注释一致：1=代取药，2=代取报告...）
                    String optionDesc = getSpecialRequirementDesc(optionInt);
                    if (optionDesc != null) {
                        sb.append(optionDesc).append("、");
                    }
                }
                if (sb.length() > 0) {
                    sb.setLength(sb.length() - 1);
                }
                if (order.getCustomRequirement() != null && !order.getCustomRequirement().trim().isEmpty()) {
                    sb.append("；").append(order.getCustomRequirement());
                }
                order.setSpecialRequirements(sb.toString());
            } else {
                order.setSpecialRequirements(order.getCustomRequirement());
            }

            // 金额计算逻辑不变
            if (order.getConsultationDuration() != null && order.getUnitPrice() != null) {
                BigDecimal estimatedAmount = order.getConsultationDuration().multiply(order.getUnitPrice());
                order.setOrderAmount(estimatedAmount);

                BigDecimal prepayRate = new BigDecimal("0.3");
                order.setDepositAmount(estimatedAmount.multiply(prepayRate).setScale(2, RoundingMode.HALF_UP));
            }

            Order createdOrder = orderService.createOrder(order);
            Map<String, Object> result = new HashMap<>();
            result.put("orderId", createdOrder.getOrderId());
            result.put("verificationCode", createdOrder.getVerificationCode());
            result.put("orderInfo", createdOrder);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("订单创建异常", e);
            return ResponseEntity.internalServerError().body("订单创建异常：" + e.getMessage());
        }
    }

    @PostMapping(value = "/appointment", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "创建预约订单", notes = "创建预约类订单，返回预约结果")
    public String createAppointmentOrder(@RequestBody Order order) {
        logger.info("创建预约订单: {}", order);
        int result = orderService.createAppointmentOrder(order);
        return result > 0 ? "预约成功" : "预约失败";
    }

    @PostMapping(value = "/evaluation", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "更新订单评价", notes = "提交或修改订单的评价内容")
    public String updateOrderEvaluation(@RequestBody Order order) {
        logger.info("更新订单评价: {}", order);
        int result = orderService.updateOrderEvaluation(order);
        return result > 0 ? "评价更新成功" : "评价更新失败";
    }

    @PostMapping(value = "/attendant/star", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "更新陪诊师星级", notes = "对陪诊师服务进行星级评分（1-5星）")
    public String updateAttendantStarRating(@RequestBody Order order) {
        logger.info("更新陪诊师星级: {}", order);
        int result = orderService.updateAttendantStarRating(order);
        return result > 0 ? "星级更新成功" : "星级更新失败";
    }

    @GetMapping("/clinic-types")
    @ApiOperation(value = "获取所有就诊类型", notes = "返回系统支持的所有就诊类型列表（1=普通陪诊等）")
    public List<Map<String, String>> getAllClinicTypes() {
        logger.info("获取所有陪诊类型");
        return orderService.getAllClinicTypes();
    }

    @GetMapping("/{orderId}")
    @ApiOperation(value = "根据ID查询订单", notes = "通过订单ID查询详细信息")
    @ApiImplicitParam(
            name = "orderId",
            value = "订单ID",
            required = true,
            dataType = "int",
            paramType = "path",
            example = "1001"
    )
    public ResponseEntity<Order> getOrderById(@PathVariable Integer orderId) {
        logger.info("获取订单 {}", orderId);
        Order order = orderService.getById(orderId);
        return ResponseEntity.ok(order);
    }


    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "更新订单信息", notes = "全量更新订单信息，支持高频需求选择 + 自定义补充")
    public ResponseEntity<Integer> updateOrder(@RequestBody Order order) {
        try {
            // 合并选择项和自定义需求：同createOrder逻辑，int值转描述
            if (order.getSelectedOptions() != null && !order.getSelectedOptions().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (Integer optionInt : order.getSelectedOptions()) { // 原String → Integer
                    String optionDesc = getSpecialRequirementDesc(optionInt);
                    if (optionDesc != null) {
                        sb.append(optionDesc).append("、");
                    }
                }
                if (sb.length() > 0) {
                    sb.setLength(sb.length() - 1);
                }
                if (order.getCustomRequirement() != null && !order.getCustomRequirement().trim().isEmpty()) {
                    sb.append("；").append(order.getCustomRequirement());
                }
                order.setSpecialRequirements(sb.toString());
            } else {
                order.setSpecialRequirements(order.getCustomRequirement());
            }

            // 金额计算逻辑不变
            if (order.getConsultationDuration() != null && order.getUnitPrice() != null) {
                BigDecimal estimatedAmount = order.getConsultationDuration().multiply(order.getUnitPrice());
                order.setOrderAmount(estimatedAmount);

                BigDecimal prepayRate = new BigDecimal("0.3");
                order.setDepositAmount(estimatedAmount.multiply(prepayRate).setScale(2, RoundingMode.HALF_UP));
            }

            int result = orderService.updateOrder(order);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("订单更新异常", e);
            return ResponseEntity.internalServerError().body(-1);
        }
    }

    @PostMapping(value = "/{orderId}/upload-medical-record", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "上传电子病历", notes = "为指定订单上传电子病历文件")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "orderId",
                    value = "订单ID",
                    required = true,
                    dataType = "int",
                    paramType = "path",
                    example = "1001"
            ),
            @ApiImplicitParam(
                    name = "file",
                    value = "电子病历文件（PDF/图片等）",
                    required = true,
                    dataType = "file",
                    paramType = "form"
            )
    })
    public ResponseEntity<?> uploadMedicalRecord(
            @PathVariable Integer orderId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) {
        String contentType = request.getContentType();
        if (contentType == null || !contentType.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            return ResponseEntity.badRequest().body("请使用 multipart/form-data 格式上传文件");
        }

        logger.info("上传订单 {} 的电子病历", orderId);
        try {
            String filePath = orderService.uploadMedicalRecord(orderId, file);
            return ResponseEntity.ok("上传成功，文件路径: " + filePath);
        } catch (Exception e) {
            logger.error("电子病历上传失败", e);
            return ResponseEntity.badRequest().body("上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/confirm")
    @ApiOperation(value = "陪诊师确认接单", notes = "陪诊师确认接单后生成陪诊码")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "orderId",
                    value = "订单ID",
                    required = true,
                    dataType = "int",
                    paramType = "query",
                    example = "1001"
            ),
            @ApiImplicitParam(
                    name = "attendantId",
                    value = "陪诊师ID",
                    required = true,
                    dataType = "int",
                    paramType = "query",
                    example = "1001"
            )
    })
    public String confirmOrder(
            @RequestParam Integer orderId,
            @RequestParam Integer attendantId
    ) {
        logger.info("陪诊师 {} 确认接单，订单ID: {}", attendantId, orderId);
        return orderService.confirmOrder(orderId, attendantId);
    }

    @GetMapping("/get-verification-code")
    @ApiOperation(value = "用户获取陪诊码", notes = "仅在陪诊师确认接单后可获取")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "orderId",
                    value = "订单ID",
                    required = true,
                    dataType = "int",
                    paramType = "query",
                    example = "1001"
            ),
            @ApiImplicitParam(
                    name = "userId",
                    value = "用户ID（验证归属）",
                    required = true,
                    dataType = "int",
                    paramType = "query",
                    example = "1001"
            )
    })
    public String getVerificationCode(
            @RequestParam Integer orderId,
            @RequestParam Integer userId
    ) {
        logger.info("用户 {} 获取订单 {} 的陪诊码", userId, orderId);
        return orderService.getVerificationCode(orderId, userId);
    }

    @PostMapping("/verify-code")
    @ApiOperation(value = "校验陪诊验证码", notes = "仅验证验证码是否正确")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderId", value = "订单ID", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "verificationCode", value = "用户输入的验证码", required = true, dataType = "String", paramType = "query")
    })
    public String verifyCode(
            @RequestParam Integer orderId,
            @RequestParam String verificationCode
    ) {
        logger.info("校验订单 {} 的验证码: {}", orderId, verificationCode);
        return orderService.verifyVerificationCode(orderId, verificationCode);
    }

    /**
     * 特殊需求int值对应描述（与实体类注释一致）
     * 1=代取药，2=代取报告，3=需轮椅协助，4=复查，5=药物过敏
     */
    private String getSpecialRequirementDesc(Integer optionInt) {
        return switch (optionInt) {
            case 1 -> "代取药";
            case 2 -> "代取报告";
            case 3 -> "需轮椅协助";
            case 4 -> "复查";
            case 5 -> "药物过敏";
            default -> {
                logger.warn("无效的特殊需求int值：{}", optionInt);
                yield null;
            }
        };
    }
}