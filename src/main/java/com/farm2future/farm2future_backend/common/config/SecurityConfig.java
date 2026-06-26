package com.farm2future.farm2future_backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关闭 CSRF，方便前后端分离项目调用接口
                .csrf(csrf -> csrf.disable())

                // 开启 CORS 支持
                .cors(cors -> {})

                // 关闭默认表单登录页面
                .formLogin(form -> form.disable())

                // 关闭浏览器 Basic 登录弹窗
                .httpBasic(basic -> basic.disable())

                // 临时放行所有请求
                // 后面接入 JWT 后再改成部分接口需要登录
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/dashboard/**",
                                "/actuator/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
