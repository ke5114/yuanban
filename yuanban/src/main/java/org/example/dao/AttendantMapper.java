package org.example.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.model.Attendant;

import java.util.List;

@Mapper
public interface AttendantMapper {
    // 注册陪诊师(需插入简介字段)
    int register(Attendant attendant);

    // 根据用户名查询陪诊师（查询结果包含简介）
    Attendant findByUsername(String username);

    // 根据ID查询陪诊师（查询结果包含简介）
    Attendant findById(Integer id);

    // 查询所有陪诊师（查询结果包含简介）
    List<Attendant> findAll();

    // 更新陪诊师信息（需过呢更新简介字段）
    int update(Attendant attendant);

    // 删除陪诊师（不涉及简介字段）
    int delete(Integer id);

    // 检查用户名是否存在
    int checkUsernameExists(String username);


    Attendant findByUserId(Integer userId);

    int insert(Attendant attendant);
}