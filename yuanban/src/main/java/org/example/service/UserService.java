package org.example.service;

import org.example.common.ResponseResult;
import org.example.model.User;

import java.util.List;
import java.util.Map;

public interface UserService {

    ResponseResult<Map<String, Object>> wechatLogin(String code);

    ResponseResult<Map<String, Object>> wechatPhoneLogin(String code, String encryptedData, String iv);

    // 原有方法保持不变...
 boolean existsByUsername(String username);
 User findById(Integer id);
 int save(User user);
 int update(User user);
 int delete(Integer id);
 int register(User user);
 boolean checkUsernameExists(User user);
 User login(String username, String password);
 List<User> findAll();
 User findByUsername(String username);
 List<User> findUsersByCondition(String condition);
    List<Map<String, Object>> getAllUserTypes();
 User getUserById(Integer userId);

}
