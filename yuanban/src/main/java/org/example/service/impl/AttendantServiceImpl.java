package org.example.service.impl;

import org.example.dao.AttendantMapper;
import org.example.dao.OrderMapper;
import org.example.model.Attendant;
import org.example.model.Order;
import org.example.model.User;
import org.example.service.AttendantService;
import org.example.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AttendantServiceImpl implements AttendantService {
    private static final Logger logger = LoggerFactory.getLogger(AttendantServiceImpl.class);

    @Autowired
    private AttendantMapper attendantMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderMapper orderMapper;


    @Override
    @Transactional
    public int register(Attendant attendant) {
        // 1. 校验用户ID
        if (attendant.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 2. 检查用户是否存在
        User user = userService.findById(attendant.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 3. 检查是否已注册为陪诊师
        if (attendantMapper.findByUserId(attendant.getUserId()) != null) {
            throw new IllegalArgumentException("该用户已注册为陪诊师");
        }

        // 4. 注册陪诊师
        return attendantMapper.register(attendant);
    }

    @Override
    public int register(Attendant attendant, Integer userId) {
        return 0; // 若无需此方法，可后续根据需求删除或完善
    }

    @Override
    public Attendant login(String username, String password) {
        try {
            // 根据用户名查询陪诊师
            Attendant attendant = attendantMapper.findByUsername(username);

            // 密码校验
            if (attendant != null && password.equals(attendant.getPassword())) {
                logger.info("陪诊师登录成功，用户名：{}", username);
                return attendant;
            } else {
                logger.warn("陪诊师登录失败，用户名或密码错误：{}", username);
                return null;
            }

        } catch (Exception e) {
            logger.error("陪诊师登录查询失败，用户名：{}，异常：{}", username, e);
            return null;
        }
    }

    @Override
    public Order createOrder(Order order) {
        return null;
    }

    @Override
    public void updatePaymentStatus(Long orderId, String status) {
        Order order = new Order();
        order.setOrderId(Math.toIntExact(orderId));

        // 根据字符串status，转换为对应的Integer值
        if ("PENDING".equals(status)) {
            order.setPaymentStatus(0); // "待支付" 对应 int 0
        } else if ("PAID".equals(status)) {
            order.setPaymentStatus(1); // "已支付" 对应 int 1
        } else {
            // 可选：处理无效状态（如设默认值、抛异常）
            order.setPaymentStatus(0);
        }

        orderMapper.updateByPrimaryKeySelective(order);
    }

    @Override
    public Attendant findByUsername(String username) {
        return attendantMapper.findByUsername(username);
    }

    @Override
    public Attendant findById(Integer id) {
        return attendantMapper.findById(id);
    }

    @Override
    public List<Attendant> findAll() {
        return attendantMapper.findAll();
    }

    @Override
    public int update(Attendant attendant) {
        return attendantMapper.update(attendant);
    }

    @Override
    public int delete(Integer id) {
        return attendantMapper.delete(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        int count = attendantMapper.checkUsernameExists(username);
        return count > 0;
    }

    @Override
    public Attendant getAttendantById(Integer attendantId) {
        // 可根据需求补充：return attendantMapper.findById(attendantId);
        return null;
    }

    @Override
    public boolean existsByUserId(Long userId) {
        // 可根据需求补充：return attendantMapper.findByUserId(Math.toIntExact(userId)) != null;
        return false;
    }
}