package com.example.hotelai.controller;

import com.example.hotelai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*") // 复用现有跨域配置，也可统一在WebConfig中配置
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册接口
     * @param request 请求参数（username/password/phone/email）
     * @return 注册结果
     */
    @PostMapping("/register")
    public Map<String, String> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String phone = request.get("phone");
        String email = request.get("email");
        return userService.register(username, password, phone, email);
    }

    /**
     * 用户登录接口
     * @param request 请求参数（username/password）
     * @return 登录结果（含JWT令牌）
     */
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        return userService.login(username, password);
    }
}