package com.example.hotelai;

import com.example.hotelai.controller.UserController;
import com.example.hotelai.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    // 用 Mockito 生成假的 UserService
    @Mock
    private UserService userService;

    // 自动把假的 UserService 注入到 UserController 中
    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // 初始化 Mock
    }

    // ===== 注册测试 =====

    @Test
    void register_Success() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "zhangsan");
        request.put("password", "123456");

        Map<String, String> serviceReturn = new HashMap<>();
        serviceReturn.put("code", "200");
        serviceReturn.put("message", "注册成功");
        serviceReturn.put("username", "zhangsan");

        when(userService.register("zhangsan", "123456", null, null))
                .thenReturn(serviceReturn);

        Map<String, String> result = userController.register(request);

        assertEquals("200", result.get("code"));
        assertEquals("注册成功", result.get("message"));
        assertEquals("zhangsan", result.get("username"));
    }

    @Test
    void register_UserExists() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "zhangsan");
        request.put("password", "123456");

        Map<String, String> serviceReturn = new HashMap<>();
        serviceReturn.put("code", "500");
        serviceReturn.put("message", "用户名已存在");

        when(userService.register("zhangsan", "123456", null, null))
                .thenReturn(serviceReturn);

        Map<String, String> result = userController.register(request);

        assertEquals("500", result.get("code"));
        assertEquals("用户名已存在", result.get("message"));
    }

    // ===== 登录测试 =====

    @Test
    void login_Success() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "zhangsan");
        request.put("password", "123456");

        Map<String, String> serviceReturn = new HashMap<>();
        serviceReturn.put("code", "200");
        serviceReturn.put("message", "登录成功");
        serviceReturn.put("token", "jwt_token_xxx");
        serviceReturn.put("username", "zhangsan");

        when(userService.login("zhangsan", "123456")).thenReturn(serviceReturn);

        Map<String, String> result = userController.login(request);

        assertEquals("200", result.get("code"));
        assertEquals("登录成功", result.get("message"));
        assertEquals("jwt_token_xxx", result.get("token"));
    }

    @Test
    void login_WrongPassword() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "zhangsan");
        request.put("password", "wrong");

        Map<String, String> serviceReturn = new HashMap<>();
        serviceReturn.put("code", "500");
        serviceReturn.put("message", "密码错误");

        when(userService.login("zhangsan", "wrong")).thenReturn(serviceReturn);

        Map<String, String> result = userController.login(request);

        assertEquals("500", result.get("code"));
        assertEquals("密码错误", result.get("message"));
    }
}