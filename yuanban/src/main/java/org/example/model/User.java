package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "用户实体类，包含登录及基本信息")
public class User {
    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("userId")
    @ApiModelProperty(value = "用户ID", example = "1001", position = 1)
    private Integer id;

    @Setter
    @Getter
    @ApiModelProperty(value = "姓名", example = "微信用户", position = 2)
    private String name;

    @Getter
    @Setter
    @ApiModelProperty(value = "年龄", example = "25", position = 3)
    private Integer age;

    @Setter
    @Getter
    @ApiModelProperty(value = "性别", example = "女", allowableValues = "男,女", position = 4)
    private String sex;

    @Getter
    @Setter
    @ApiModelProperty(value = "用户名（可选）", example = "wx_user123", position = 5)
    private String username;

    @Getter
    @Setter
    @ApiModelProperty(value = "密码（可选）", hidden = true)
    private String password;

    @Getter
    @Setter
    @ApiModelProperty(value = "联系电话", example = "13900139000", position = 7)
    private String phone;

    @Setter
    @Getter
    @ApiModelProperty(value = "登录成功状态", example = "true", position = 8)
    private boolean isLoginSuccess;

    @Setter
    @Getter
    @ApiModelProperty(value = "登录提示信息", example = "登录成功", position = 9)
    private String loginMessage;

    @Getter
    @Setter
    @ApiModelProperty (value = "用户类型（0 = 普通用户，1 = 陪诊师）", example = "0", position = 10)
    private Integer userType;

    @Getter
    @Setter
    @ApiModelProperty(value = "微信唯一标识", position = 11)
    private String openid;


    public User() {}

    public User(String openid) {
        this.openid = openid;
        this.userType = 0;  // 正确引用内部枚举
        this.username = "wx_" + openid.substring(0, 8);
    }

    public class UserType {
    }
}
