package com.farm2future.farm2future_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 跨域配置类。
 *
 * <p>
 * CORS 全称是 Cross-Origin Resource Sharing，
 * 中文通常叫“跨域资源共享”。
 * </p>
 *
 * <p>
 * 当前后端接口和前端页面不在同一个源时，
 * 浏览器会触发跨域限制。
 * 例如：
 * </p>
 *
 * <pre>
 * 前端地址：http://localhost:5173
 * 后端地址：http://localhost:8080
 * </pre>
 *
 * <p>
 * 虽然它们都在本机，但端口不同，也属于不同源，
 * 所以需要后端允许跨域访问。
 * </p>
 */
@Configuration
public class CorsConfig {

    /**
     * 创建 CORS 配置源。
     *
     * <p>
     * 该 Bean 会被 Spring Security 或 Spring MVC 使用，
     * 用于判断哪些前端地址、请求方法、请求头可以访问后端接口。
     * </p>
     *
     * @return CORS 配置源对象
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // 创建 CORS 配置对象
        CorsConfiguration config = new CorsConfiguration();

        /*
         * 允许访问后端接口的前端地址。
         *
         * 注意：
         * 这里必须写完整的协议 + 域名/IP + 端口。
         *
         * 例如：
         * http://localhost:5173
         *
         * 不能只写 localhost，也不能漏掉 http://。
         */
        config.setAllowedOrigins(List.of(
                // 本地前端开发地址，Vite 默认端口
                "http://localhost:5173",

                // 本地前端开发地址的 IP 写法
                "http://127.0.0.1:5173",

                // 服务器部署后的前端地址
                "http://64.176.57.254"
        ));

        /*
         * 允许前端使用的 HTTP 请求方法。
         *
         * GET    ：查询数据
         * POST   ：新增数据、登录
         * PUT    ：整体更新数据
         * PATCH  ：局部更新数据
         * DELETE ：删除数据
         * OPTIONS：浏览器跨域预检请求
         */
        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        /*
         * 允许前端携带的请求头。
         *
         * 这里使用 "*" 表示允许所有请求头，
         * 例如：
         * Content-Type
         * Authorization
         */
        config.setAllowedHeaders(List.of("*"));

        /*
         * 允许前端读取的响应头。
         *
         * 默认情况下，浏览器不能读取所有响应头。
         * 如果后端在响应头中返回 Authorization，
         * 需要在这里暴露给前端。
         */
        config.setExposedHeaders(List.of("Authorization"));

        /*
         * 是否允许携带 Cookie、Session 等凭证信息。
         *
         * 当前项目使用 JWT Token 认证，
         * Token 通常放在 Authorization 请求头中，
         * 所以这里设置为 false 是可以的。
         *
         * 如果以后要使用 Cookie 登录，
         * 这里需要改成 true，
         * 并且 allowedOrigins 不能使用 "*"。
         */
        config.setAllowCredentials(false);

        // 创建基于 URL 的 CORS 配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        /*
         * 将上面的 CORS 配置应用到所有接口路径。
         *
         * "/**" 表示后端所有接口都使用这套跨域规则。
         */
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}