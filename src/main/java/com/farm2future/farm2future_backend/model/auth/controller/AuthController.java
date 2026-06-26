package com.farm2future.farm2future_backend.model.auth.controller;

import com.farm2future.farm2future_backend.model.auth.dto.LoginRequest;
import com.farm2future.farm2future_backend.model.auth.dto.LoginResponse;
import com.farm2future.farm2future_backend.model.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器。
 *
 * <p>
 * 该 Controller 负责处理用户认证相关接口，
 * 例如用户登录、获取 Token 等。
 * </p>
 *
 * <p>
 * 当前接口基础路径为：
 * </p>
 *
 * <pre>
 * /api/auth
 * </pre>
 *
 * <p>
 * 例如登录接口完整路径为：
 * </p>
 *
 * <pre>
 * POST /api/auth/login
 * </pre>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * 认证业务服务。
     *
     * <p>
     * 具体的登录校验逻辑不写在 Controller 中，
     * 而是交给 Service 层处理。
     * 这样可以保持 Controller 简洁，
     * 也符合企业级项目的分层设计。
     * </p>
     */
    private final AuthService authService;

    /**
     * 用户登录接口。
     *
     * <p>
     * 前端提交邮箱和密码后，
     * 后端会校验用户是否存在、密码是否正确。
     * 如果校验成功，则返回 JWT Token 和用户基础信息。
     * </p>
     *
     * <p>
     * 请求示例：
     * </p>
     *
     * <pre>
     * POST /api/auth/login
     *
     * {
     *   "email": "admin@farm2future.com",
     *   "password": "123456"
     * }
     * </pre>
     *
     * <p>
     * {@code @Valid} 表示会触发 LoginRequest 中的参数校验规则。
     * 如果参数不合法，会被 GlobalExceptionHandler 统一处理。
     * </p>
     *
     * @param request 登录请求参数，包含邮箱和密码
     * @return 登录响应结果，通常包含 Token 和用户信息
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        // 调用 Service 层完成登录校验和 Token 生成
        return authService.login(request);
    }
}