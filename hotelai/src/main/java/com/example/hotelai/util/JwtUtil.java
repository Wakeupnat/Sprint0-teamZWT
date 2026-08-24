package com.example.hotelai.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    // JWT密钥（建议配置在application.properties中）
    @Value("${jwt.secret:hotelai_jwt_secret_key_1234567890}")
    private String secret;

    // JWT过期时间（单位：毫秒，这里配置2小时）
    @Value("${jwt.expiration:7200000}")
    private long expiration;

    // 生成JWT令牌（适配0.12.5版本，移除了过时的SignatureAlgorithm）
    public String generateToken(String username) {
        // 生成密钥（新版无需指定算法，默认HS256）
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .subject(username) // 新版简化了setSubject为subject
                .issuedAt(new Date()) // 签发时间
                .expiration(new Date(System.currentTimeMillis() + expiration)) // 过期时间
                .signWith(key) // 新版无需手动指定SignatureAlgorithm，自动适配密钥算法
                .compact();
    }

    // 从JWT令牌中提取用户名
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    // 验证JWT令牌是否有效
    public boolean validateToken(String token, String username) {
        String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    // 检查JWT是否过期
    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    // 解析JWT获取Claims（适配0.12.5版本的解析方式）
    private Claims getClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parser() // 新版parserBuilder简化为parser
                .verifyWith(key) // 新版用verifyWith替代setSigningKey
                .build()
                .parseSignedClaims(token) // 新版用parseSignedClaims替代parseClaimsJws
                .getPayload(); // 新版用getPayload替代getBody
    }
}