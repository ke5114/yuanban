package org.example.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.example.common.ResponseResult;
import org.example.model.User;
import org.example.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("api/users")
@Api(tags = "用户管理接口", description = "包含用户注册、登录、信息管理等操作")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;

    @GetMapping
    @ApiOperation(value = "查询所有用户", notes = "返回系统中所有用户的列表")
    public ResponseResult<List<User>> getAllUsers() {
        try {
            List<User> users = userService.findAll();
            return new ResponseResult<>(200, "查询成功", users);
        } catch (Exception e) {
            logger.error("查询用户列表失败", e);
            return new ResponseResult<>(500, "查询用户列表失败，服务器内部错误", null);
        }
    }

    @PostMapping
    @ApiOperation(value = "创建用户", notes = "新增普通用户（需包含用户名、密码等必填信息）")
    @ApiImplicitParam(name = "user", value = "用户信息", required = true, dataType = "User", paramType = "body")
    public int createUser(@RequestBody User user) {
        logger.info("接收到的用户对象: {}", user);
        try {
            return userService.register(user);
        } catch (Exception e) {
            logger.error("创建用户失败", e);
            throw new RuntimeException("创建用户失败");
        }
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据ID查询用户", notes = "通过用户ID查询详细信息")
    @ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "Integer", paramType = "path")
    public User getUser(@PathVariable Integer id) {
        try {
            return userService.findById(id);
        } catch (Exception e) {
            logger.error("获取用户失败", e);
            throw new RuntimeException("获取用户失败");
        }
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "更新用户信息", notes = "根据ID更新用户信息（部分字段可空）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "user", value = "更新后的用户信息", required = true, dataType = "User", paramType = "body")
    })
    public int updateUser(@PathVariable Integer id, @RequestBody User user) {
        try {
            user.setId(id);
            return userService.update(user);
        } catch (Exception e) {
            logger.error("更新用户失败", e);
            throw new RuntimeException("更新用户失败");
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除用户", notes = "根据ID删除指定用户")
    @ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "Integer", paramType = "path")
    public void deleteUser(@PathVariable Integer id) {
        try {
            userService.delete(id);
        } catch (Exception e) {
            logger.error("删除用户失败", e);
            throw new RuntimeException("删除用户失败");
        }
    }

    @PostMapping("/register")
    @ApiOperation(value = "用户注册", notes = "新用户注册（用户名需唯一）")
    @ApiImplicitParam(name = "user", value = "注册信息（包含用户名、密码等）", required = true, dataType = "User", paramType = "body")
    public ResponseResult<Integer> register(
            @RequestBody User user,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            String errorMessage = Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage();
            return new ResponseResult<>(400, errorMessage, null);
        }
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            return new ResponseResult<>(400, "用户名不能为空", null);
        }
        // 新增：用户名长度不少于两个字符（长度 < 2 时返回错误）
        if (user.getUsername().length() < 2) {
            return new ResponseResult<>(400, "用户名长度不能少于两个字符", null);
        }
        try {
            // 注册默认普通用户（userType=0）
            if (user.getUserType() == null) {
                user.setUserType(0);
            }
            int userId = userService.register(user);
            return new ResponseResult<>(200, "注册成功", userId);
        } catch (IllegalArgumentException e) {
            logger.warn("用户名 {} 已存在", user.getUsername());
            return new ResponseResult<>(400, e.getMessage(), null);
        } catch (Exception e) {
            logger.error("用户注册异常", e);
            return new ResponseResult<>(500, "注册失败，服务器内部错误", null);
        }
    }

    @PostMapping("/login")
    @ApiOperation(value = "用户登录", notes = "通过用户名和密码登录，返回Token和用户类型（0=普通用户，1=陪诊师）")
    @ApiImplicitParam(name = "user", value = "登录信息（用户名+密码）", required = true, dataType = "User", paramType = "body")
    public ResponseResult<Map<String, Object>> login(
            @RequestBody User user,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return new ResponseResult<>(400, bindingResult.getFieldError().getDefaultMessage(), null);
        }
        try {
            User loggedUser = userService.login(user.getUsername(), user.getPassword());
            if (loggedUser != null) {
                String token = generateToken(loggedUser.getUsername());
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", loggedUser.getId());
                // 原枚举.name() → 直接返回int值（0=普通用户，1=陪诊师）
                userInfo.put("userType", loggedUser.getUserType());
                return new ResponseResult<>(200, "登录成功", Map.of("token", token, "userInfo", userInfo));
            }
            return new ResponseResult<>(401, "用户名或密码错误", null);
        } catch (Exception e) {
            logger.error("登录异常: {}", user.getUsername(), e);
            return new ResponseResult<>(500, "服务器内部错误", null);
        }
    }

    @PostMapping("/wechat/login")
    @ApiOperation(value = "微信授权登录", notes = "通过微信code获取用户信息并登录")
    @ApiImplicitParam(name = "code", value = "微信临时授权code", required = true, dataType = "String", paramType = "query")
    public ResponseResult<Map<String, Object>> wechatLogin(@RequestParam String code) {
        return userService.wechatLogin(code);
    }

    @PostMapping("/wechat/login/phone")
    @ApiOperation(value = "微信授权获取手机号登录", notes = "通过微信加密数据获取手机号并登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "微信临时code", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "encryptedData", value = "加密的手机号数据", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "iv", value = "解密向量", required = true, dataType = "String", paramType = "query")
    })
    public ResponseResult<Map<String, Object>> wechatPhoneLogin(
            @RequestParam String code,
            @RequestParam String encryptedData,
            @RequestParam String iv) {
        return userService.wechatPhoneLogin(code, encryptedData, iv);
    }

    @GetMapping("/checkUsername")
    @ApiOperation(value = "检查用户名是否存在", notes = "用于注册时验证用户名唯一性")
    @ApiImplicitParam(name = "username", value = "待检查的用户名", required = true, dataType = "String", paramType = "query")
    public boolean checkUsername(@RequestParam String username) {
        return userService.findByUsername(username) != null;
    }

    @GetMapping("/search")
    @ApiOperation(value = "搜索用户", notes = "根据姓名或用户名模糊搜索用户")
    @ApiImplicitParam(name = "condition", value = "搜索关键词", required = true, dataType = "String", paramType = "query")
    public List<User> searchUsers(@RequestParam String condition) {
        return userService.findUsersByCondition(condition);
    }

    @GetMapping("/types")
    @ApiOperation(value = "获取所有用户类型", notes = "返回系统支持的用户类型（0=普通用户，1=陪诊师）")
    public ResponseResult<List<Map<String, Object>>> getAllUserTypes() {
        try {
            // 原枚举列表 → 固定int+描述（无需查数据库，与实体类注释一致）
            List<Map<String, Object>> result = new ArrayList<>();

            Map<String, Object> normalUser = new HashMap<>();
            normalUser.put("value", 0); // 普通用户
            normalUser.put("label", "普通用户");
            result.add(normalUser);

            Map<String, Object> attendantUser = new HashMap<>();
            attendantUser.put("value", 1); // 陪诊师
            attendantUser.put("label", "陪诊师");
            result.add(attendantUser);

            return new ResponseResult<>(200, "查询成功", result);
        } catch (Exception e) {
            logger.error("查询用户类型失败", e);
            return new ResponseResult<>(500, "查询用户类型失败，服务器内部错误", null);
        }
    }

    private String generateToken(String username) {
        return username;
    }
}