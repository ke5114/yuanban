package org.example.service.impl;

import com.alibaba.fastjson.JSONObject;
//import org.example.Data.WechatDecryptUtil;
import org.example.Data.WechatDecryptUtil;
import org.example.common.ResponseResult;
import org.example.dao.UserMapper;
import org.example.model.User;
import org.example.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceMybatisImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceMybatisImpl.class);
    private final JdbcTemplate jdbcTemplate;
    private final UserMapper userMapper;
    private final RestTemplate restTemplate;

    // 微信配置参数
    @Value("${wechat.appid}")
    private String appid;
    @Value("${wechat.secret}")
    private String secret;
    @Value("${wechat.code2session.url}")
    private String code2SessionUrl;

    @Autowired
    public UserServiceMybatisImpl(JdbcTemplate jdbcTemplate, UserMapper userMapper, RestTemplate restTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.userMapper = userMapper;
        this.restTemplate = restTemplate;
    }


    @Override
    public ResponseResult<Map<String, Object>> wechatLogin(String code) {
        try {
            // 1. 调用微信接口换取openid
            String url = String.format(code2SessionUrl, appid, secret, code);
            String response = restTemplate.getForObject(url, String.class);
            logger.info("微信code2session响应: {}", response);

            // 2. 解析微信返回结果
            JSONObject result = JSONObject.parseObject(response);
            String openid = result.getString("openid");
            String errMsg = result.getString("errmsg");

            // 校验微信接口返回是否异常
            if (openid == null || !"ok".equals(errMsg)) {
                return new ResponseResult<>(400, "微信授权失败: " + errMsg, null);
            }

            // 3. 根据openid查询用户是否已存在
            User user = userMapper.findByOpenid(openid);
            if (user == null) {
                // 3.1 新用户：默认普通用户（userType=0）
                user = new User(openid);
                user.setUserType(0); // 原枚举User.USER → int 0
                userMapper.save(user);
                logger.info("微信新用户注册成功，openid: {}", openid);
            } else {
                logger.info("微信老用户登录成功，openid: {}", openid);
            }

            // 4. 生成登录Token
            String token = generateToken(user.getId().toString());

            // 5. 构建返回结果：用户类型返回int（0=普通用户，1=陪诊师）
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("userType", user.getUserType()); // 原枚举.name() → int值
            userInfo.put("openid", user.getOpenid());

            Map<String, Object> loginResult = new HashMap<>();
            loginResult.put("token", token);
            loginResult.put("userInfo", userInfo);

            return new ResponseResult<>(200, "微信登录成功", loginResult);

        } catch (Exception e) {
            logger.error("微信登录异常", e);
            return new ResponseResult<>(500, "微信登录失败", null);
        }
    }


    @Override
    public ResponseResult<Map<String, Object>> wechatPhoneLogin(String code, String encryptedData, String iv) {
        try {
            // 1. 调用微信接口获取session_key和openid
            String url = String.format(code2SessionUrl, appid, secret, code);
            String response = restTemplate.getForObject(url, String.class);
            logger.info("微信code2session响应: {}", response);

            JSONObject result = JSONObject.parseObject(response);
            String openid = result.getString("openid");
            String sessionKey = result.getString("session_key");
            String errMsg = result.getString("errmsg");

            if (openid == null || sessionKey == null || !"ok".equals(errMsg)) {
                return new ResponseResult<>(400, "微信授权失败: " + errMsg, null);
            }

            // 2. 解密手机号
            String decryptedPhoneJson = WechatDecryptUtil.decryptPhone(encryptedData, sessionKey, iv);
            JSONObject phoneInfo = JSONObject.parseObject(decryptedPhoneJson);
            String phoneNumber = phoneInfo.getString("phoneNumber");
            logger.info("解密成功，手机号: {}", phoneNumber);

            // 3. 用户查询与创建：默认普通用户（userType=0）
            User user = userMapper.findByPhone(phoneNumber);
            if (user != null) {
                // 绑定openid（如果未绑定）
                if (user.getOpenid() == null || user.getOpenid().isEmpty()) {
                    user.setOpenid(openid);
                    userMapper.update(user);
                }
            } else {
                // 检查openid是否已注册
                user = userMapper.findByOpenid(openid);
                if (user != null) {
                    // 绑定手机号
                    user.setPhone(phoneNumber);
                    userMapper.update(user);
                } else {
                    // 全新用户：创建普通用户
                    user = new User(openid);
                    user.setPhone(phoneNumber);
                    user.setName("微信用户_" + phoneNumber.substring(7));
                    user.setUserType(0); // 原枚举User.USER → int 0
                    userMapper.save(user);
                }
            }

            // 4. 生成Token并返回：用户类型int值
            String token = generateToken(user.getId().toString());
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("userType", user.getUserType()); // 原枚举.name() → int值
            userInfo.put("phone", user.getPhone());

            Map<String, Object> loginResult = new HashMap<>();
            loginResult.put("token", token);
            loginResult.put("userInfo", userInfo);

            return new ResponseResult<>(200, "微信手机号登录成功", loginResult);

        } catch (Exception e) {
            logger.error("微信手机号登录异常", e);
            return new ResponseResult<>(500, "登录失败：" + e.getMessage(), null);
        }
    }


    @Override
    public boolean existsByUsername(String username) {
        logger.info("开始检查用户名 {} 是否存在", username);
        User user = findByUsername(username);
        boolean result = user != null;
        logger.info("用户名 {} 是否存在: {}", username, result);
        return result;
    }

    @Override
    public User findById(Integer id) {
        return userMapper.findById(id);
    }

    @Override
    public int save(User user) {
        return userMapper.save(user);
    }

    @Override
    public int update(User user) {
        return userMapper.update(user);
    }

    @Override
    public int delete(Integer id) {
        return userMapper.delete(id);
    }

    @Transactional
    @Override
    public int register(User user) {
        if (existsByUsername(user.getUsername())) {
            logger.warn("用户名 {} 已存在，拒绝注册", user.getUsername());
            throw new IllegalArgumentException("用户名已存在，不能重复注册");
        }
        try {
            // 注册默认普通用户（userType=0）
            if (user.getUserType() == null) {
                user.setUserType(0);
            }
            logger.info("开始执行用户 {} 的注册操作，用户信息: {}", user.getUsername(), user);
            userMapper.register(user);
            logger.info("用户 {} 注册成功，用户 ID: {}", user.getUsername(), user.getId());
            return user.getId();
        } catch (Exception e) {
            logger.error("用户注册时发生异常，用户信息: {}", user, e);
            throw e;
        }
    }

    @Override
    public boolean checkUsernameExists(User user) {
        int count = userMapper.checkUsernameExists(user);
        logger.info("查询用户名 {} 存在的数量为: {}", user.getUsername(), count);
        return count > 0;
    }


    @Override
    public User login(String username, String password) {
        logger.info("开始处理登录请求，用户名: {}", username);

        User user;
        try {
            user = findByUsername(username);
        } catch (Exception e) {
            logger.error("查询用户信息失败，用户名: {}", username, e);
            return null;
        }

        if (user == null) {
            logger.warn("用户不存在，用户名: {}", username);
            return null;
        }
        logger.info("查询到用户基本信息: {}", user.getUsername());

        if (user.getPassword().equals(password)) {
            logger.info("密码匹配成功，登录校验通过");

            // 补全用户类型：默认普通用户（0）
            if (user.getUserType() == null) {
                user.setUserType(0); // 原枚举User.USER → int 0
            }

            return user;
        } else {
            logger.warn("密码不匹配，登录失败，用户名: {}", username);
            return null;
        }
    }

    @Override
    public List<User> findAll() {
        return userMapper.findAll();
    }

    @Override
    public User findByUsername(String username) {
        List<User> userList = userMapper.findByUsername(username);
        return userList.isEmpty() ? null : userList.get(0);
    }

    @Override
    public List<User> findUsersByCondition(String condition) {
        return userMapper.findUsersByCondition(condition);
    }


    @Override
    public List<Map<String, Object>> getAllUserTypes() {
        List<Map<String, Object>> userTypes = new ArrayList<>();

        Map<String, Object> normalUser = new HashMap<>();
        normalUser.put("value", 0); // 0 代表普通用户
        normalUser.put("label", "普通用户");
        userTypes.add(normalUser);

        Map<String, Object> attendantUser = new HashMap<>();
        attendantUser.put("value", 1); // 1 代表陪诊师
        attendantUser.put("label", "陪诊师");
        userTypes.add(attendantUser);

        return userTypes;
    }
    @Override
    public User getUserById(Integer userId) {
        return userMapper.findById(userId);
    }

    // -------------------------- 原有不变方法 --------------------------
    private String generateToken(String userId) {
        // 实际项目建议用JWT实现
        return userId + "_" + System.currentTimeMillis();
    }
}