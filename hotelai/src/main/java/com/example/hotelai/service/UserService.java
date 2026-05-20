package com.example.hotelai.service;

import com.example.hotelai.entity.User;
import java.util.Map;

public interface UserService {
    // 用户注册
    Map<String, String> register(String username, String password, String phone, String email);

    // 用户登录（返回JWT令牌）
    Map<String, String> login(String username, String password);
}