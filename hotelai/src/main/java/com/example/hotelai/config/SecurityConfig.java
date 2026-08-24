package com.example.hotelai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())               // 禁用 CSRF（方便测试）
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()               // 允许所有请求，无需认证
                )
                .httpBasic(httpBasic -> httpBasic.disable()) // 禁用 HTTP Basic 认证
                .formLogin(form -> form.disable());          // 禁用表单登录
        return http.build();
    }
}