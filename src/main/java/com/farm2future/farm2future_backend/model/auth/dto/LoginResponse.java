package com.farm2future.farm2future_backend.model.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应 DTO。
 *
 * <p>
 * DTO 全称是 Data Transfer Object，
 * 用于封装后端返回给前端的数据。
 * </p>
 *
 * <p>
 * 当前类对应登录接口成功后的响应结果。
 * 登录成功后，后端会返回当前用户信息和 JWT Token。
 * </p>
 *
 * <pre>
 * {
 *   "user": {
 *     "id": "1",
 *     "name": "Admin User",
 *     "role": "admin",
 *     "entityName": "Farm2Future"
 *   },
 *   "token": "xxxxx.yyyyy.zzzzz"
 * }
 * </pre>
 */
@Data
@AllArgsConstructor
public class LoginResponse {

    /**
     * 当前登录用户的基础信息。
     *
     * <p>
     * 该字段用于前端展示用户名称、角色、所属实体等信息。
     * </p>
     */
    private UserProfile user;

    /**
     * JWT Token。
     *
     * <p>
     * 前端登录成功后需要保存该 Token，
     * 后续访问需要登录权限的接口时，
     * 需要在请求头中携带该 Token。
     * </p>
     *
     * <pre>
     * Authorization: Bearer xxxxx.yyyyy.zzzzz
     * </pre>
     */
    private String token;

    /**
     * 用户基础信息 DTO。
     *
     * <p>
     * 用于返回给前端的用户资料。
     * 注意这里不要返回密码、密码哈希、删除状态等敏感或内部字段。
     * </p>
     */
    @Data
    @AllArgsConstructor
    public static class UserProfile {

        /**
         * 用户 ID。
         *
         * <p>
         * 用户在数据库中的唯一标识。
         * </p>
         */
        private String id;

        /**
         * 用户名称。
         *
         * <p>
         * 用于前端显示当前登录用户的名字。
         * </p>
         */
        private String name;

        /**
         * 用户角色。
         *
         * <p>
         * 用于前端判断当前用户身份，
         * 例如 admin、farmer、auditor、supplier。
         * </p>
         */
        private String role;

        /**
         * 用户所属实体名称。
         *
         * <p>
         * 例如农场名称、供应商名称、审核机构名称等。
         * </p>
         */
        private String entityName;
    }
}