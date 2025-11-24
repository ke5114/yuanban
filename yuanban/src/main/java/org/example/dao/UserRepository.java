package org.example.dao;
import org.example.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.logging.Logger;
@Repository
public class UserRepository {
    // 数据库操作相关方法...
    @Autowired
    private JdbcTemplate jdbcTemplate;
        // 正确初始化 Logger
        private static final Logger logger = Logger.getLogger(UserRepository.class.getName());

        // 获取所有用户
        public List<User> findAll() {
            String sql = "SELECT * FROM user";
            logger.info("执行查询所有用户的 SQL: {}" );
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class));
    }

    // 根据 ID 获取用户
    public User findById(Integer id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        return jdbcTemplate.queryForObject( sql, new Object[]{id}, new BeanPropertyRowMapper<>( User.class ) );
    }


    // 更新用户
    public int update(User user) {
        String sql = "UPDATE user SET name = ?, age = ?,sex = ? WHERE id = ?";
        return jdbcTemplate.update( sql, user.getName(), user.getAge(),user.getSex(), user.getId() );
    }

    // 删除用户
    public int delete(Long id) {
        String sql = "DELETE FROM user WHERE id = ?";
        return jdbcTemplate.update( sql, id );
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM user WHERE username = ?";
        List<User> users = jdbcTemplate.query(sql, new Object[]{username}, new BeanPropertyRowMapper<>(User.class));
        return users.isEmpty()? null : users.get(0);
    }

    public int save(User user) {
        logger.info("保存用户信息: {}" );
        String sql = "INSERT INTO user (name, age, sex, username, password) VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, user.getName(), user.getAge(), user.getSex(), user.getUsername(), user.getPassword());
    }

    public int register(User user) {
        // 可添加注册前校验逻辑，如用户名是否已存在等
        return save(user);
    }
}
