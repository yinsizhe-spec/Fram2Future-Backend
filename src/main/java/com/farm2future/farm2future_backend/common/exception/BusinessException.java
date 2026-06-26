package com.farm2future.farm2future_backend.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 业务异常类。
 *
 * <p>
 * 用于在业务逻辑中主动抛出异常，例如：
 * 登录失败、用户不存在、权限不足、参数不合法等业务场景。
 * </p>
 *
 * <p>
 * 该异常会携带 HTTP 状态码、业务错误码、错误信息以及可选的详细信息，
 * 方便全局异常处理器统一返回给前端。
 * </p>
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * HTTP 状态码。
     *
     * <p>
     * 例如：
     * 400 BAD_REQUEST、
     * 401 UNAUTHORIZED、
     * 403 FORBIDDEN、
     * 404 NOT_FOUND。
     * </p>
     */
    private final HttpStatus status;

    /**
     * 业务错误码。
     *
     * <p>
     * 用于前端或日志中区分不同类型的业务错误。
     * 例如：USER_NOT_FOUND、INVALID_PASSWORD、TOKEN_EXPIRED。
     * </p>
     */
    private final String code;

    /**
     * 异常详细信息。
     *
     * <p>
     * 可以存放额外的错误数据，例如字段校验错误、参数详情等。
     * 如果不需要详细信息，可以为 null。
     * </p>
     */
    private final Object details;

    /**
     * 创建一个不带详细信息的业务异常。
     *
     * @param status  HTTP 状态码
     * @param code    业务错误码
     * @param message 错误提示信息
     */
    public BusinessException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = null;
    }

    /**
     * 创建一个带详细信息的业务异常。
     *
     * @param status  HTTP 状态码
     * @param code    业务错误码
     * @param message 错误提示信息
     * @param details 异常详细信息
     */
    public BusinessException(HttpStatus status, String code, String message, Object details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details;
    }
}