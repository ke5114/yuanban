package org.example.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "陪诊师实体类，包含基本信息和资质信息")
public class Attendant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "陪诊师ID", example = "1", position = 1)
    private Integer id;         // 主键ID

    @ApiModelProperty(value = "姓名", required = true, example = "张三", position = 2)
    private String name;        // 姓名

    @ApiModelProperty(value = "年龄", example = "30", position = 3)
    private Integer age;        // 年龄

    @ApiModelProperty(value = "性别", example = "男", allowableValues = "男,女", position = 4)
    private String sex;         // 性别

    @ApiModelProperty(value = "登录用户名（唯一）", required = true, example = "attendant123", position = 5)
    private String username;    // 登录用户名

    @ApiModelProperty(value = "登录密码（加密存储）", required = true, example = "加密后的密码", position = 6)
    private String password;    // 登录密码

    @ApiModelProperty(value = "联系电话", example = "13800138000", position = 7)
    private String phone;       // 联系电话

    @ApiModelProperty(value = "资格证书编号", example = "MED2023001", position = 8)
    private String certificate; // 资格证书编号

    @ApiModelProperty(value = "状态（0:未审核，1:已审核，2:封禁）", example = "1", position = 9)
    private Integer status;     // 状态

    @ApiModelProperty(value = "陪诊师简介", example = "从事陪诊工作5年，经验丰富", position = 10)
    private String introduction;// 陪诊师简介

    public Attendant(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // 以下方法保持不变（用户ID相关，按需调整）
    public void setUserId(Integer userId) {}
    public Integer getUserId() { return null; }
}