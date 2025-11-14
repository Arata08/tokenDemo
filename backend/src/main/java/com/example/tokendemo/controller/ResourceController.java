package com.example.tokendemo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/resource")
public class ResourceController {

    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getData(HttpServletRequest request) {
        String currentUser = (String) request.getAttribute("currentUser");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "这是受保护的资源数据");
        response.put("currentUser", currentUser);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}