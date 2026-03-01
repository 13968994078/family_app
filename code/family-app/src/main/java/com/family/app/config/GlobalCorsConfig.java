package com.family.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 标记为配置类，Spring 自动加载
public class GlobalCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 对所有接口生效（可改为 /api/** 仅对 API 接口生效）
                .allowedOrigins("http://localhost:5173") // 允许的前端域名（开发环境）
                // 生产环境改为具体域名，如：.allowedOrigins("https://your-frontend.com")
                // 若需允许多个域名：.allowedOrigins("域名1", "域名2")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的请求方法
                .allowedHeaders("*") // 允许所有请求头（简化配置，生产环境可指定具体头）
                .allowCredentials(true) // 允许传递 Cookie/Token（前端需配合 withCredentials: true）
                .maxAge(3600); // 预检请求（OPTIONS）的缓存时间（1小时，减少重复预检）
    }
}