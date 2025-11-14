package com.example.tokendemo.filter;

import com.example.tokendemo.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // 对于登录和刷新token接口，不需要验证token
        if (request.getRequestURI().startsWith("/api/auth")) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // 移除 "Bearer " 前缀
            try {
                Claims claims = jwtUtil.parseAccessToken(token);
                // 可以将用户信息存入 SecurityContext 或 Request Attributes
                String username = claims.getSubject();
                // SecurityContextHolder.getContext().setAuthentication(...);
                request.setAttribute("currentUser", username);
                return true; // 继续执行后续拦截器和控制器
            } catch (RuntimeException e) {
                // Token 无效，返回 401
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Unauthorized\"}");
                return false; // 中断执行
            }
        } else {
            // 没有提供token，返回401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Unauthorized\"}");
            return false; // 中断执行
        }
    }
}