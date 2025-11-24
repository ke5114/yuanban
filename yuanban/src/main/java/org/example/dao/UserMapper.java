package org.example.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.model.User;

import java.util.List;

/**
 * 建mapper接口，必须是接口，加上注解，表示这是连接数据库的类
 */
@Mapper
public interface UserMapper {
    int register(User user);

    // 🔥 关键修改：删除@Select注解，避免与XML映射冲突
    User findById(Integer id);

    // 插入用户
    int save(User user);

    // 更新用户
    int update(User user);

    // 删除用户
    int delete(Integer id);

    // 根据用户名查询用户
    List<User> findByUsername(String username);

    // 插入新用户（注册）
    List<User> findAll();

    // 根据条件查询用户（示例，可按需调整参数）
    List<User> findUsersByCondition(String condition);

    int checkUsernameExists(User user);

    // 查询所有用户类型
    List<String> getAllUserTypes();

    // 【新增】通过openid查询用户
    User findByOpenid(String openid);

    User findByPhone(String phoneNumber);

}