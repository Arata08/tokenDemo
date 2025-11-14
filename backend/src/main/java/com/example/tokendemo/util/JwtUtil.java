package com.example.tokendemo.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private SecretKey getAccessTokenKey() {
        // 注意：生产中应从配置文件读取，并且密钥要足够复杂且保密
        String ACCESS_TOKEN_SECRET = "awvrbasbttbxbhchnchnthntncnhavawvavaw";
        return Keys.hmacShaKeyFor(ACCESS_TOKEN_SECRET.getBytes());
    }

    private SecretKey getRefreshTokenKey() {
        String REFRESH_TOKEN_SECRET = "rznrzrnyxncthtnhctnctnnntnttvawvavrwavavw";
        return Keys.hmacShaKeyFor(REFRESH_TOKEN_SECRET.getBytes());
    }

    // 生成 Access Token
    public String generateAccessToken(String username) {
        Date now = new Date();
        // 30秒，为了演示效果设短一些
        long ACCESS_TOKEN_EXPIRATION = 30 * 1000;
        Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(getAccessTokenKey())
                .compact();
    }

    // 生成 Refresh Token
    public String generateRefreshToken(String username) {
        Date now = new Date();
        // 2分钟
        long REFRESH_TOKEN_EXPIRATION = 2 * 60 * 1000;
        Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(getRefreshTokenKey())
                .compact();
    }

    // 解析 Access Token
    public Claims parseAccessToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getAccessTokenKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            // Token 无效或过期
            throw new RuntimeException("Invalid or expired access token", e);
        }
    }

    // 解析 Refresh Token
    public Claims parseRefreshToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getRefreshTokenKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            // Token 无效或过期
            throw new RuntimeException("Invalid or expired refresh token", e);
        }
    }

    // 验证 Access Token 是否过期
    public boolean isAccessTokenExpired(String token) {
        try {
            Claims claims = parseAccessToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (JwtException e) {
            return true; // 解析失败也视为过期
        }
    }
}