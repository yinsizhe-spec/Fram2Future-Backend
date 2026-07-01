package com.farm2future.farm2future_backend.model.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.farm2future.farm2future_backend.common.exception.BusinessException;
import com.farm2future.farm2future_backend.common.security.JwtTokenProvider;
import com.farm2future.farm2future_backend.model.auth.dto.LoginRequest;
import com.farm2future.farm2future_backend.model.auth.dto.LoginResponse;
import com.farm2future.farm2future_backend.model.user.entity.AppUser;
import com.farm2future.farm2future_backend.model.user.mapper.AppUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 认证业务服务类。
 *
 * <p>
 * 该类负责处理用户认证相关的核心业务逻辑，
 * 例如登录校验、密码比对、角色校验、JWT Token 生成等。
 * </p>
 *
 * <p>
 * Controller 只负责接收请求和返回响应，
 * 真正的业务判断应该放在 Service 层中完成。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    /**
     * 用户表 Mapper。
     *
     * <p>
     * 用于根据邮箱、角色等条件查询用户数据。
     * </p>
     */
    private final AppUserMapper appUserMapper;

    /**
     * 密码加密与校验工具。
     *
     * <p>
     * 当前项目使用 BCrypt 保存密码。
     * 登录时不会直接比较明文密码，
     * 而是使用 {@code passwordEncoder.matches()} 判断明文密码
     * 是否与数据库中的密码哈希匹配。
     * </p>
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * JWT Token 工具类。
     *
     * <p>
     * 登录成功后，用于生成返回给前端的 JWT Token。
     * </p>
     */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 用户登录。
     *
     * <p>
     * 登录流程：
     * </p>
     *
     * <ol>
     *     <li>标准化邮箱和角色。</li>
     *     <li>校验角色是否合法。</li>
     *     <li>根据邮箱、角色、删除状态查询用户。</li>
     *     <li>校验密码是否正确。</li>
     *     <li>生成 JWT Token。</li>
     *     <li>封装用户资料和 Token 返回给前端。</li>
     * </ol>
     *
     * @param request 登录请求参数，包含 email、password、role
     * @return 登录成功响应，包含用户基础信息和 JWT Token
     */
    public LoginResponse login(LoginRequest request) {
        /*
         * 标准化邮箱：
         * 1. 去掉前后空格
         * 2. 转成小写
         *
         * 这样可以避免用户输入 Admin@xxx.com 和 admin@xxx.com
         * 导致查询结果不一致。
         */
        String email = request.getEmail().trim().toLowerCase();

        /*
         * 标准化角色：
         * 1. 去掉前后空格
         * 2. 转成小写
         *
         * 例如前端传 ADMIN，也可以统一转换成 admin 处理。
         */
        String role = request.getRole().trim().toLowerCase();

        // 校验角色是否在系统允许范围内
        validateRole(role);

        /*
         * 根据邮箱、角色和逻辑删除状态查询用户。
         *
         * deleted = 0 表示用户没有被逻辑删除。
         * last("LIMIT 1") 表示最多只取一条数据。
         */
        AppUser user = appUserMapper.selectOne(
                new LambdaQueryWrapper<AppUser>()
                        .eq(AppUser::getEmail, email)
                        .eq(AppUser::getRole, role)
                        .eq(AppUser::getDeleted, 0)
                        .last("LIMIT 1")
        );

        /*
         * 如果用户不存在，不明确告诉前端是邮箱错还是角色错。
         *
         * 这样可以避免攻击者通过错误信息枚举系统中的用户账号。
         */
        if (user == null) {
            throwInvalidCredentials();
        }

        /*
         * 校验密码。
         *
         * request.getPassword() 是前端传来的明文密码；
         * user.getPassword() 是数据库中保存的 BCrypt 密码哈希。
         */
        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        // 密码不匹配时，抛出统一的登录失败异常
        if (!passwordMatches) {
            throwInvalidCredentials();
        }

        // 登录成功后，根据用户信息生成 JWT Token
        String token = jwtTokenProvider.generateToken(user);

        /*
         * 构造返回给前端的用户资料。
         *
         * 注意：
         * 这里不要返回 password、deleted 等敏感或内部字段。
         */
        LoginResponse.UserProfile profile = new LoginResponse.UserProfile(
                user.getId(),
                user.getName(),
                user.getRole(),
                user.getEntityName()
        );

        // 返回登录成功结果
        return new LoginResponse(profile, token);
    }

    /**
     * 校验用户角色是否合法。
     *
     * <p>
     * 当前系统允许的登录角色只有：
     * farmer、regulator、admin。
     * </p>
     *
     * @param role 前端传入并标准化后的角色
     */
    private void validateRole(String role) {
        /*
         * 判断角色是否为空。
         *
         * 虽然 LoginRequest 中已经使用了 @NotBlank，
         * 这里再次校验可以提高 Service 层的健壮性。
         */
        if (!StringUtils.hasText(role)) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "VALIDATION_ERROR",
                    "Role is required."
            );
        }

        /*
         * 判断角色是否在允许范围内。
         *
         * 如果以后接口文档增加新角色，
         * 例如 supplier、auditor，
         * 这里也需要同步扩展。
         */
        if (!role.equals("farmer") && !role.equals("regulator") && !role.equals("buyer")) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "VALIDATION_ERROR",
                    "Role must be Farmer, Buyer, or Regulator."
            );
        }
    }

    /**
     * 抛出登录失败异常。
     *
     * <p>
     * 邮箱不存在、角色不匹配、密码错误时，
     * 都统一返回相同的错误信息。
     * </p>
     *
     * <p>
     * 这样做更安全，可以避免向外部暴露具体是哪一项登录信息错误。
     * </p>
     */
    private void throwInvalidCredentials() {
        throw new BusinessException(
                HttpStatus.UNAUTHORIZED,
                "INVALID_CREDENTIALS",
                "Login failed. Please check your email, password, and role."
        );
    }
}