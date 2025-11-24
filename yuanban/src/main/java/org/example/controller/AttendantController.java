package org.example.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.example.common.ResponseResult;
import org.example.dao.AttendantMapper;
import org.example.model.Attendant;
import org.example.model.User;
import org.example.service.AttendantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("api/attendants")
@Api(tags = "陪诊师管理接口", description = "包含陪诊师注册、登录、查询等操作")
public class AttendantController {
    private static final Logger logger = LoggerFactory.getLogger(AttendantController.class);

    @Autowired
    private AttendantService attendantService;
    private AttendantMapper userService;
    private Object jwtUtil;

    @PostMapping("/register")
    @ApiOperation(value = "陪诊师注册", notes = "需携带用户Token，创建陪诊师信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "attendantDTO", value = "陪诊师信息", required = true, dataType = "Attendant", paramType = "body"),
            @ApiImplicitParam(name = "Authorization", value = "用户Token", required = true, dataType = "String", paramType = "header")
    })
    public ResponseResult<Attendant> registerAttendant(
            @RequestBody Attendant attendantDTO,
            @RequestHeader("Authorization") String token
    ) {
        // 原有业务逻辑不变
        try {
            Integer userId = 1; // 示例：实际从Token解析
            Attendant user = userService.findById(userId);
            if (user == null) {
                return new ResponseResult<>(400, "用户不存在", null);
            }
            if (attendantService.existsByUserId(userId.longValue())) {
                return new ResponseResult<>(400, "该用户已注册为陪诊师", null);
            }
            Attendant attendant = new Attendant();
            attendant.setUserId(userId);
            attendant.setCertificate(attendantDTO.getCertificate());
            attendant.setIntroduction(attendantDTO.getIntroduction());
            attendantService.register(attendant);
            return new ResponseResult<>(200, "陪诊师注册成功", attendant);
        } catch (Exception e) {
            logger.error("陪诊师注册失败", e);
            return new ResponseResult<>(500, "服务器内部错误", null);
        }
    }

    @PostMapping("/login")
    @ApiOperation(value = "陪诊师登录", notes = "通过用户名和密码登录，返回Token和用户信息")
    @ApiImplicitParam(name = "attendant", value = "登录信息（用户名+密码）", required = true, dataType = "Attendant", paramType = "body")
    public ResponseResult<Map<String, Object>> login(
            @Valid @RequestBody Attendant attendant,
            BindingResult bindingResult
    ) {
        // 原有业务逻辑不变
        if (bindingResult.hasErrors()) {
            String error = bindingResult.getFieldError().getDefaultMessage();
            return new ResponseResult<>(400, "登录失败：" + error, null);
        }
        try {
            Attendant loggedAttendant = attendantService.login(attendant.getUsername(), attendant.getPassword());
            if (loggedAttendant != null) {
                String token = generateToken(loggedAttendant.getUsername());
                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("userInfo", loggedAttendant);
                return new ResponseResult<>(200, "登录成功", data);
            } else {
                return new ResponseResult<>(401, "用户名或密码错误", null);
            }
        } catch (Exception e) {
            logger.error("陪诊师登录失败：{}", attendant.getUsername(), e);
            return new ResponseResult<>(500, "服务器内部错误", null);
        }
    }

    @GetMapping
    @ApiOperation(value = "查询所有陪诊师", notes = "返回所有陪诊师的列表（包含简介等信息）")
    public ResponseResult<List<Attendant>> getAllAttendants() {
        // 原有业务逻辑不变
        try {
            List<Attendant> attendants = attendantService.findAll();
            return new ResponseResult<>(200, "查询成功", attendants);
        } catch (Exception e) {
            logger.error("查询陪诊师列表失败", e);
            return new ResponseResult<>(500, "服务器内部错误", null);
        }
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据ID查询陪诊师", notes = "通过ID查询单个陪诊师的详细信息")
    @ApiImplicitParam(name = "id", value = "陪诊师ID", required = true, dataType = "Integer", paramType = "path")
    public ResponseResult<Attendant> getAttendant(@PathVariable Integer id) {
        // 原有业务逻辑不变
        try {
            Attendant attendant = attendantService.findById(id);
            return attendant != null ?
                    new ResponseResult<>(200, "查询成功", attendant) :
                    new ResponseResult<>(404, "陪诊师不存在", null);
        } catch (Exception e) {
            logger.error("查询陪诊师失败", e);
            return new ResponseResult<>(500, "服务器内部错误", null);
        }
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "更新陪诊师信息", notes = "根据ID更新陪诊师的基本信息（姓名、年龄等）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "陪诊师ID", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "attendant", value = "更新后的陪诊师信息", required = true, dataType = "Attendant", paramType = "body")
    })
    public ResponseResult<Integer> updateAttendant(
            @PathVariable Integer id,
            @RequestBody Attendant attendant
    ) {
        // 原有业务逻辑不变
        try {
            Attendant existingAttendant = attendantService.findById(id);
            if (existingAttendant == null) {
                return new ResponseResult<>(404, "陪诊师不存在", 0);
            }
            attendant.setId(id);
            int result = attendantService.update(attendant);
            return new ResponseResult<>(200, "更新成功", result);
        } catch (Exception e) {
            logger.error("更新陪诊师失败", e);
            return new ResponseResult<>(500, "服务器内部错误", 0);
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除陪诊师", notes = "根据ID删除指定陪诊师")
    @ApiImplicitParam(name = "id", value = "陪诊师ID", required = true, dataType = "Integer", paramType = "path")
    public ResponseResult<Void> deleteAttendant(@PathVariable Integer id) {
        // 原有业务逻辑不变
        try {
            attendantService.delete(id);
            return new ResponseResult<>(200, "删除成功", null);
        } catch (Exception e) {
            logger.error("删除陪诊师失败", e);
            return new ResponseResult<>(500, "服务器内部错误", null);
        }
    }

    private String generateToken(String username) {
        return "ATTENDANT_TOKEN_" + username;
    }
}