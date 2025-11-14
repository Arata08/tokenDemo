package com.example.tokendemo.controller;

import com.example.tokendemo.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    // 模拟用户验证逻辑
    private boolean validateUser(String username, String password) {
        // 实际应用中应查询数据库
        return "admin".equals(username) && "password".equals(password);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        if (validateUser(username, password)) {
            String accessToken = jwtUtil.generateAccessToken(username);
            String refreshToken = jwtUtil.generateRefreshToken(username);

            Map<String, String> response = new HashMap<>();
            response.put("access_token", accessToken);
            response.put("refresh_token", refreshToken);
            response.put("token_type", "Bearer");

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(null); // 或返回错误信息
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestHeader("Authorization") String refreshTokenHeader) {
        if (refreshTokenHeader == null || !refreshTokenHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String refreshToken = refreshTokenHeader.substring(7); // 移除 "Bearer " 前缀

        try {
            Claims claims = jwtUtil.parseRefreshToken(refreshToken);
            String username = claims.getSubject();
            // 可在此处进行额外的刷新 Token 有效性检查，例如查询数据库黑名单等

            // 生成新的 Access Token
            String newAccessToken = jwtUtil.generateAccessToken(username);
            // (可选) 同时生成新的 Refresh Token 并返回，旧的刷新 Token 应该被废弃
            // String newRefreshToken = jwtUtil.generateRefreshToken(username);

            Map<String, String> response = new HashMap<>();
            response.put("access_token", newAccessToken);
            // response.put("refresh_token", newRefreshToken); // 如果更新了
            response.put("token_type", "Bearer");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Refresh Token 无效或过期
            return ResponseEntity.status(401).build();
        }
    }
}