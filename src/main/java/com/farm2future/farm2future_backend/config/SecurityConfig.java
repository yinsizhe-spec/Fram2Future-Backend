package com.farm2future.farm2future_backend.config;

import com.farm2future.farm2future_backend.common.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 安全配置类。
 *
 * <p>
 * 该类用于配置系统的认证和授权规则，
 * 包括哪些接口可以匿名访问、哪些接口必须登录后访问、
 * 是否启用 JWT 过滤器、是否关闭 Session 等。
 * </p>
 *
 * <p>
 * 当前项目采用 JWT Token 认证方式，
 * 因此后端不依赖传统的 Session 登录状态。
 * 每次请求都需要前端在请求头中携带 Token。
 * </p>
 *
 * <pre>
 * Authorization: Bearer xxxxx.yyyyy.zzzzz
 * </pre>
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * JWT 认证过滤器。
     *
     * <p>
     * 用于在请求进入 Controller 之前，
     * 解析请求头中的 JWT Token，
     * 并将用户认证信息放入 Spring Security 上下文。
     * </p>
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置 Spring Security 的过滤器链。
     *
     * <p>
     * 这里定义了项目整体的安全规则：
     * </p>
     *
     * <ol>
     *     <li>启用 CORS 跨域配置。</li>
     *     <li>关闭 CSRF 防护。</li>
     *     <li>关闭 Session，使用无状态认证。</li>
     *     <li>允许登录接口匿名访问。</li>
     *     <li>允许 actuator 健康检查接口匿名访问。</li>
     *     <li>其他接口必须登录后访问。</li>
     *     <li>把 JWT 过滤器加入 Spring Security 过滤器链。</li>
     * </ol>
     *
     * @param http Spring Security 的 HTTP 安全配置对象
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                /*
                 * 启用 CORS。
                 *
                 * 这里会使用项目中定义的 CorsConfigurationSource，
                 * 也就是 CorsConfig 里的跨域配置。
                 */
                .cors(Customizer.withDefaults())

                /*
                 * 关闭 CSRF。
                 *
                 * CSRF 主要用于防止基于 Cookie / Session 的跨站请求伪造攻击。
                 * 当前项目使用 JWT Token，并且后端是无状态接口，
                 * 所以通常可以关闭 CSRF。
                 */
                .csrf(csrf -> csrf.disable())

                /*
                 * 设置 Session 策略为 STATELESS。
                 *
                 * 表示后端不创建、不使用 Session。
                 * 每次请求是否登录，都通过 JWT Token 判断。
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                /*
                 * 配置接口访问权限。
                 */
                .authorizeHttpRequests(auth -> auth

                        /*
                         * 登录接口允许匿名访问。
                         *
                         * 因为用户还没有登录时，需要先调用该接口获取 Token。
                         */
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        /*
                         * actuator 接口允许匿名访问。
                         *
                         * 常用于健康检查，例如：
                         * /actuator/health
                         *
                         * 如果以后部署到生产环境，
                         * 可以考虑只开放 health，或者增加访问限制。
                         */
                        .requestMatchers("/actuator/**").permitAll()

                        /*
                         * 其他所有接口都必须登录后才能访问。
                         *
                         * 如果请求没有携带合法 Token，
                         * Spring Security 会返回 401 或 403。
                         */
                        .anyRequest().authenticated()
                )

                /*
                 * 将 JWT 认证过滤器加入到 Spring Security 过滤器链中。
                 *
                 * 必须放在 UsernamePasswordAuthenticationFilter 之前，
                 * 这样请求到达用户名密码认证过滤器之前，
                 * JWT 就已经完成了解析和认证。
                 */
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                /*
                 * 构建最终的 SecurityFilterChain 对象。
                 */
                .build();
    }

    /**
     * 密码加密器。
     *
     * <p>
     * 使用 BCrypt 算法对用户密码进行加密和校验。
     * 企业级项目中，数据库里不能保存明文密码，
     * 应该保存 BCrypt 加密后的密码哈希值。
     * </p>
     *
     * <p>
     * 常见用途：
     * </p>
     *
     * <pre>
     * passwordEncoder.encode("123456")
     * passwordEncoder.matches(rawPassword, encodedPassword)
     * </pre>
     *
     * @return BCrypt 密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器。
     *
     * <p>
     * AuthenticationManager 是 Spring Security 的认证入口，
     * 可以用于执行用户名密码认证等操作。
     * </p>
     *
     * <p>
     * 当前项目如果登录逻辑是自己查询数据库并校验密码，
     * 这个 Bean 暂时可能用不到；
     * 但保留它可以方便后续扩展标准 Spring Security 登录流程。
     * </p>
     *
     * @param configuration Spring Security 自动提供的认证配置对象
     * @return AuthenticationManager 认证管理器
     * @throws Exception 获取认证管理器失败时抛出异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}