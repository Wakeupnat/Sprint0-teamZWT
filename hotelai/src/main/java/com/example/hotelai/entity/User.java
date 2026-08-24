package com.example.hotelai.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "USERS") // Oracle表名建议大写，匹配JPA注解
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "USERNAME", unique = true, nullable = false, length = 50)
    private String username; // 用户名（唯一）

    @Column(name = "PASSWORD", nullable = false, length = 200)
    private String password; // 加密后的密码

    @Column(name = "PHONE", length = 20)
    private String phone; // 手机号（可选）

    @Column(name = "EMAIL", length = 100)
    private String email; // 邮箱（可选）

    @Column(name = "STATUS", nullable = false)
    private Integer status = 1; // 1-正常，0-禁用
}