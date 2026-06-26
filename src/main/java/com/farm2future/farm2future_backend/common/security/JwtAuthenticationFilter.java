package com.farm2future.farm2future_backend.common.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.farm2future.farm2future_backend.model.user.entity.AppUser;
import com.farm2future.farm2future_backend.model.user.mapper.AppUserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器。
 *
 * <p>
 * 该过滤器会在每一次 HTTP 请求进入 Controller 之前执行，
 * 用于从请求头中读取 JWT Token，并解析出当前登录用户信息。
 * </p>
 *
 * <p>
 * 如果 Token 合法，并且用户在数据库中存在，
 * 就会把用户信息写入 Spring Security 的上下文中。
 * 后续接口就可以通过 Spring Security 判断当前用户是否已登录、
 * 以及当前用户拥有哪些角色权限。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * JWT 工具类。
     *
     * <p>
     * 用于校验 Token 是否有效，以及从 Token 中解析用户 ID。
     * </p>
     */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 用户表 Mapper。
     *
     * <p>
     * 用于根据 Token 中解析出的用户 ID 查询数据库中的用户信息。
     * </p>
     */
    private final AppUserMapper appUserMapper;

    /**
     * 每次请求都会执行的过滤逻辑。
     *
     * <p>
     * 主要流程：
     * </p>
     *
     * <ol>
     *     <li>从请求头 Authorization 中获取 Token。</li>
     *     <li>判断 Token 是否以 Bearer 开头。</li>
     *     <li>校验 Token 是否有效。</li>
     *     <li>从 Token 中解析用户 ID。</li>
     *     <li>根据用户 ID 查询数据库用户。</li>
     *     <li>构造 Spring Security 认证对象。</li>
     *     <li>把认证对象保存到 SecurityContext 中。</li>
     * </ol>
     *
     * @param request     当前 HTTP 请求对象
     * @param response    当前 HTTP 响应对象
     * @param filterChain 过滤器链，用于继续执行后续过滤器
     * @throws ServletException Servlet 异常
     * @throws IOException      IO 异常
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 从请求头中获取 Authorization，例如：Bearer xxxxx.yyyyy.zzzzz
        String authHeader = request.getHeader("Authorization");

        // 如果没有 Authorization 请求头，或者格式不是 Bearer Token，则直接放行
        // 是否能访问接口，交给后面的 Spring Security 配置决定
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 去掉 Bearer 前缀，只保留真正的 JWT Token 字符串
        String token = authHeader.substring(7);

        // 校验 Token 是否有效
        // 如果 Token 无效，不设置登录状态，直接放行
        if (!jwtTokenProvider.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 从 Token 中解析出用户 ID
        String userId = jwtTokenProvider.getUserId(token);

        // 根据用户 ID 查询数据库用户
        // 同时要求 deleted = 0，表示用户未被逻辑删除
        AppUser user = appUserMapper.selectOne(
                new LambdaQueryWrapper<AppUser>()
                        .eq(AppUser::getId, userId)
                        .eq(AppUser::getDeleted, 0)
        );

        // 如果 Token 中的用户 ID 在数据库中不存在，则不设置认证信息，直接放行
        if (user == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 构造用户角色权限
        // Spring Security 默认要求角色权限以 ROLE_ 开头
        // 例如数据库中 role = admin，这里会转换成 ROLE_ADMIN
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase());

        // 构造 Spring Security 的认证对象
        // 第一个参数是当前登录用户信息
        // 第二个参数是密码，这里不需要，所以传 null
        // 第三个参数是当前用户拥有的权限集合
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(authority)
                );

        // 将认证对象保存到 Spring Security 上下文中
        // 后续接口就可以识别当前请求已经登录
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 继续执行后续过滤器和 Controller
        filterChain.doFilter(request, response);
    }
}