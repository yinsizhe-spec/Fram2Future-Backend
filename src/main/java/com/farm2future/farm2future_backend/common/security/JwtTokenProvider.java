package com.farm2future.farm2future_backend.common.security;

import com.farm2future.farm2future_backend.model.user.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT Token 工具类。
 *
 * <p>
 * 该类负责 JWT 的生成、解析和校验。
 * 登录成功后，系统会通过该类生成 Token 返回给前端；
 * 前端后续请求接口时，需要把 Token 放到请求头 Authorization 中。
 * </p>
 *
 * <p>
 * 常见请求头格式：
 * </p>
 *
 * <pre>
 * Authorization: Bearer xxxxx.yyyyy.zzzzz
 * </pre>
 *
 * <p>
 * 该类通常会被 {@code AuthService} 和 {@code JwtAuthenticationFilter} 使用。
 * </p>
 */
@Component
public class JwtTokenProvider {

    /**
     * JWT 签名密钥。
     *
     * <p>
     * 从配置文件 application.yml 或 application.properties 中读取。
     * 配置项名称为：
     * </p>
     *
     * <pre>
     * app.jwt.secret
     * </pre>
     *
     * <p>
     * 注意：密钥长度不能太短，否则不安全。
     * 当前代码要求至少 32 个字符。
     * </p>
     */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /**
     * JWT 过期时间，单位为小时。
     *
     * <p>
     * 从配置文件中读取：
     * </p>
     *
     * <pre>
     * app.jwt.expire-hours
     * </pre>
     *
     * <p>
     * 如果配置文件中没有设置该值，则默认使用 24 小时。
     * </p>
     */
    @Value("${app.jwt.expire-hours:24}")
    private Long expireHours;

    /**
     * JWT 签名使用的密钥对象。
     *
     * <p>
     * 由字符串形式的 jwtSecret 转换而来，
     * 用于生成 Token 和校验 Token 签名。
     * </p>
     */
    private SecretKey secretKey;

    /**
     * 初始化 JWT 密钥。
     *
     * <p>
     * 该方法会在 Spring 创建 Bean 后自动执行。
     * 主要作用是校验配置中的 JWT 密钥是否合法，
     * 并将字符串密钥转换成 {@link SecretKey} 对象。
     * </p>
     *
     * <p>
     * 如果 jwtSecret 为空，或者长度小于 32 个字符，
     * 程序启动时会直接抛出异常，防止使用不安全的 JWT 配置。
     * </p>
     */
    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters.");
        }

        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 根据用户信息生成 JWT Token。
     *
     * <p>
     * Token 中会保存用户的基础信息，例如：
     * 用户 ID、邮箱、角色、姓名、所属实体名称等。
     * </p>
     *
     * <p>
     * 注意：
     * Token 中不应该保存密码、身份证号、银行卡号等敏感信息。
     * </p>
     *
     * @param user 当前登录成功的用户对象
     * @return 生成后的 JWT Token 字符串
     */
    public String generateToken(AppUser user) {
        // 当前时间
        Instant now = Instant.now();

        // Token 过期时间
        Instant expiry = now.plusSeconds(expireHours * 3600);

        return Jwts.builder()
                // subject 通常用于存放用户唯一标识，这里存放用户 ID
                .subject(user.getId())

                // 自定义 Claims，用于存放业务需要的用户信息
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .claim("name", user.getName())
                .claim("entityName", user.getEntityName())

                // Token 签发时间
                .issuedAt(Date.from(now))

                // Token 过期时间
                .expiration(Date.from(expiry))

                // 使用密钥进行签名，防止 Token 被篡改
                .signWith(secretKey)

                // 生成最终的 Token 字符串
                .compact();
    }

    /**
     * 解析 JWT Token。
     *
     * <p>
     * 该方法会校验 Token 的签名，
     * 如果 Token 被篡改、格式错误或签名不匹配，会抛出异常。
     * </p>
     *
     * @param token JWT Token 字符串
     * @return Token 中的 Claims 数据
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                // 使用同一个密钥校验 Token 签名
                .verifyWith(secretKey)
                .build()

                // 解析带签名的 JWT
                .parseSignedClaims(token)

                // 获取 Token 中真正保存的数据
                .getPayload();
    }

    /**
     * 校验 JWT Token 是否有效。
     *
     * <p>
     * 主要校验内容包括：
     * </p>
     *
     * <ol>
     *     <li>Token 格式是否正确。</li>
     *     <li>Token 签名是否正确。</li>
     *     <li>Token 是否已经过期。</li>
     * </ol>
     *
     * @param token JWT Token 字符串
     * @return 如果 Token 有效返回 true，否则返回 false
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);

            // 判断 Token 过期时间是否晚于当前时间
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            // Token 解析失败、签名错误、过期等情况都会返回 false
            return false;
        }
    }

    /**
     * 从 JWT Token 中获取用户 ID。
     *
     * <p>
     * 用户 ID 存放在 Token 的 subject 字段中。
     * </p>
     *
     * @param token JWT Token 字符串
     * @return 用户 ID
     */
    public String getUserId(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从 JWT Token 中获取用户角色。
     *
     * <p>
     * 用户角色存放在自定义 Claim：role 中。
     * </p>
     *
     * @param token JWT Token 字符串
     * @return 用户角色
     */
    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }
}