package com.example.hotelai.service.impl;

import com.example.hotelai.entity.User;
import com.example.hotelai.repository.UserRepository;
import com.example.hotelai.service.UserService;
import com.example.hotelai.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // 密码加密器（Spring Security提供）
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Map<String, String> register(String username, String password, String phone, String email) {
        Map<String, String> result = new HashMap<>();

        // 1. 校验参数
        if (username == null || username.trim().isEmpty()) {
            result.put("code", "500");
            result.put("message", "用户名不能为空");
            return result;
        }
        if (password == null || password.length() < 6) {
            result.put("code", "500");
            result.put("message", "密码长度不能少于6位");
            return result;
        }

        // 2. 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            result.put("code", "500");
            result.put("message", "用户名已存在");
            return result;
        }

        // 3. 创建用户（密码加密）
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // 密码加密
        user.setPhone(phone);
        user.setEmail(email);
        user.setStatus(1); // 默认启用

        // 4. 保存用户到Oracle数据库
        userRepository.save(user);

        // 5. 返回注册成功结果
        result.put("code", "200");
        result.put("message", "注册成功");
        result.put("username", username);
        return result;
    }

    @Override
    public Map<String, String> login(String username, String password) {
        Map<String, String> result = new HashMap<>();

        // 1. 校验参数
        if (username == null || password == null) {
            result.put("code", "500");
            result.put("message", "用户名或密码不能为空");
            return result;
        }

        // 2. 查询用户
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            result.put("code", "500");
            result.put("message", "用户名不存在");
            return result;
        }

        User user = userOpt.get();

        // 3. 验证密码（加密后对比）
        if (!passwordEncoder.matches(password, user.getPassword())) {
            result.put("code", "500");
            result.put("message", "密码错误");
            return result;
        }

        // 4. 检查用户状态
        if (user.getStatus() != 1) {
            result.put("code", "500");
            result.put("message", "账号已禁用");
            return result;
        }

        // 5. 生成JWT令牌
        String token = jwtUtil.generateToken(username);

        // 6. 返回登录成功结果
        result.put("code", "200");
        result.put("message", "登录成功");
        result.put("token", token);
        result.put("username", username);
        return result;
    }
}