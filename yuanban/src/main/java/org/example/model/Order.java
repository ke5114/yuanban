package org.example.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vladmihalcea.hibernate.type.json.JsonType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Slf4j
@ApiModel(description = "订单实体类，包含预约及服务信息")
@TypeDef(name = "json", typeClass = JsonType.class)
@NoArgsConstructor
public class Order {
    @Id
    @Setter
    @Getter
    @ApiModelProperty(value = "订单ID", example = "5001", position = 1)
    @Column(name = "order_id")
    private Integer orderId;

    @Setter
    @Getter
    @ApiModelProperty(value = "订单编号（下单自动生成，唯一标识）", example = "ORD20250905123456789", position = 4)
    @Column(name = "order_no", unique = true) // 数据库唯一约束
    private String orderNo;

    @Setter
    @Getter
    @ApiModelProperty(value = "用户ID", example = "1001", position = 2)
    @Column(name = "user_id")
    private Integer userId;

    @Setter
    @Getter
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "订单日期", example = "2023-10-01 10:00:00", position = 3)
    private Date orderDate;

    @Setter
    @Getter
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "陪诊结束时间（自动计算：预约时间+陪诊时长）", example = "2023-10-05 12:30:00", position = 12)
    private Date endTime;

    @Setter
    @Getter
    @ApiModelProperty(value = "订单金额（元）", example = "300.00", position = 23)
    @Column(name = "order_amount")
    private BigDecimal orderAmount;

    @Setter
    @Getter
    @ApiModelProperty(value = "患者ID", example = "2001", position = 5)
    private Integer patientId;

    @Setter
    @Getter
    @ApiModelProperty(value = "陪诊师ID", example = "1", position = 6)
    private Integer attendantId;

    @Setter
    @Getter
    @ApiModelProperty(value = "服务地址", example = "北京市海淀区XX路", position = 7)
    private String address;

    @Setter
    @Getter
    @ApiModelProperty(value = "医院名称", example = "北京协和医院", position = 8)
    private String hospital;

    @Setter
    @Getter
    @ApiModelProperty(value = "评价内容", example = "服务很好", position = 9)
    private String comment;

    @Setter
    @Getter
    @ApiModelProperty(value = "星级评分（1-5）", example = "5", position = 10)
    private Integer starRating;

    @Setter
    @Getter
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "预约时间", example = "2023-10-05 09:30:00", position = 11)
    private Date appointmentTime;

    /**
     * 预约状态：0=待确认，1=已确认，2=已完成
     */
    @Setter
    @Getter
    @ApiModelProperty(value = "预约状态（0=待确认，1=已确认，2=已完成）", example = "0", position = 12)
    private Integer appointmentStatus;

    /**
     * 就诊类型：1=普通陪诊，2=术后护理，3=急诊陪同，4=上门陪诊
     */
    @Setter
    @Getter
    @ApiModelProperty(value = "就诊类型（1=普通陪诊等）", example = "1", position = 13)
    private Integer clinicType;

    /**
     * 支付状态：0=待支付，1=已支付
     */
    @Setter
    @Getter
    @ApiModelProperty(value = "支付状态（0=待支付，1=已支付）", example = "0", position = 14)
    private Integer paymentStatus;

    @Setter
    @Getter
    @ApiModelProperty(value = "服务内容", example = "全程陪诊+取药", position = 15)
    private String serviceContent;

    @Setter
    @Getter
    @ApiModelProperty(value = "陪诊验证码（4位数字）", example = "1234", position = 16)
    private String verificationCode;

    @Setter
    @Getter
    @ApiModelProperty(value = "电子病历路径", example = "uploads/record/123.pdf", position = 17)
    private String electronicMedicalRecord;

    @Setter
    @Getter
    @ApiModelProperty(value = "用户电话", example = "13800138000", position = 18)
    private String userPhone;

    @Setter
    @Getter
    @ApiModelProperty(value = "订单状态（0=待接单，1=已接单，2=已完成，3=已取消）", example = "0")
    @Column(name = "order_status")
    private Integer orderStatus = 0;

    @Setter
    @Getter
    @ApiModelProperty(value = "选择的高频需求选项（1=代取药等）", example = "[1,3]", position = 24)
    @Type(type = "json")
    @Column(columnDefinition = "json")
    private List<Integer> selectedOptions;

    @Setter
    @Getter
    @ApiModelProperty(value = "自定义补充需求", example = "患者对青霉素过敏")
    private String customRequirement;

    @Getter
    @Setter
    @ApiModelProperty(hidden = true)
    private String specialRequirements;

    @Setter
    @Getter
    @ApiModelProperty(value = "陪诊师电话", example = "13900139000", position = 19)
    private String attendantPhone;

    @Getter
    @Setter
    @ApiModelProperty(value = "陪诊时长（小时）", example = "3", position = 20)
    @Column(precision = 5, scale = 2)
    private BigDecimal consultationDuration;

    @Getter
    @Setter
    @ApiModelProperty(value = "预约定金（元）", example = "90.00", position = 21)
    @Column(name = "deposit_amount", precision = 10, scale = 2)
    private BigDecimal depositAmount;

    @Getter
    @Setter
    @ApiModelProperty(value = "陪诊单价（元/小时）", example = "100.00", position = 22)
    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice;

    public Integer getId() { return orderId; }

}