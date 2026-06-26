package com.farm2future.farm2future_backend.model.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求 DTO。
 *
 * <p>
 * DTO 全称是 Data Transfer Object，
 * 用于接收前端传递到后端的数据。
 * </p>
 *
 * <p>
 * 当前类对应登录接口的请求体：
 * </p>
 *
 * <pre>
 * POST /api/auth/login
 *
 * {
 *   "email": "admin@farm2future.com",
 *   "password": "123456",
 *   "role": "admin"
 * }
 * </pre>
 *
 * <p>
 * Controller 中配合 {@code @Valid} 使用时，
 * 这里的校验注解才会自动生效。
 * </p>
 */
@Data
public class LoginRequest {

    /**
     * 登录邮箱。
     *
     * <p>
     * 不能为空，并且必须符合邮箱格式。
     * </p>
     */
    @NotBlank(message = "Email is required.")
    @Email(message = "Email format is invalid.")
    private String email;

    /**
     * 登录密码。
     *
     * <p>
     * 不能为空。
     * </p>
     *
     * <p>
     * 注意：这里接收的是前端传来的明文密码，
     * 后端不会直接与数据库明文比较，
     * 而是通过 PasswordEncoder 和数据库中的 BCrypt 密码哈希进行匹配。
     * </p>
     */
    @NotBlank(message = "Password is required.")
    private String password;

    /**
     * 用户角色。
     *
     * <p>
     * 前端登录时需要传递角色，
     * 后端可以根据邮箱、密码和角色共同确认登录身份。
     * </p>
     *
     * <p>
     * 例如：
     * admin、farmer、auditor、supplier。
     * </p>
     */
    @NotBlank(message = "Role is required.")
    private String role;
}