package com.farm2future.farm2future_backend.common.exception;

import com.farm2future.farm2future_backend.common.result.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器。
 *
 * <p>
 * 用于统一处理项目中抛出的异常，并将异常转换成统一格式的 JSON 响应返回给前端。
 * </p>
 *
 * <p>
 * 通过 {@link RestControllerAdvice} 注解，
 * 该类会作用于所有 Controller，
 * 避免每个接口都重复编写 try-catch 逻辑。
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常。
     *
     * <p>
     * 当 Service 层或业务逻辑中主动抛出 {@link BusinessException} 时，
     * 会进入该方法进行统一处理。
     * </p>
     *
     * <p>
     * 例如：
     * 用户不存在、密码错误、Token 无效、业务状态不允许操作等。
     * </p>
     *
     * @param e 业务异常对象
     * @return 统一错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(ErrorResponse.of(e.getCode(), e.getMessage(), e.getDetails()));
    }

    /**
     * 处理请求参数校验异常。
     *
     * <p>
     * 当 Controller 接收的请求体对象使用了 {@code @Valid} 注解，
     * 并且字段校验失败时，会抛出 {@link MethodArgumentNotValidException}。
     * </p>
     *
     * <p>
     * 该方法会将所有字段校验错误整理成 Map，
     * 返回给前端，方便前端显示具体是哪一个字段不符合要求。
     * </p>
     *
     * @param e 参数校验异常对象
     * @return 统一错误响应，HTTP 状态码为 422
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> details = new HashMap<>();

        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of(
                        "VALIDATION_ERROR",
                        "Request body failed validation.",
                        details
                ));
    }

    /**
     * 处理权限不足异常。
     *
     * <p>
     * 当用户已经登录，但当前角色没有权限访问某个接口时，
     * Spring Security 会抛出 {@link AccessDeniedException}。
     * </p>
     *
     * <p>
     * 例如：
     * 普通用户访问管理员接口。
     * </p>
     *
     * @param e 权限不足异常对象
     * @return 统一错误响应，HTTP 状态码为 403
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(
                        "FORBIDDEN",
                        "User role not allowed for this action.",
                        null
                ));
    }

    /**
     * 处理系统未知异常。
     *
     * <p>
     * 该方法用于兜底处理所有未被上面方法捕获的异常。
     * </p>
     *
     * <p>
     * 生产环境中不建议直接把 {@code e.getMessage()} 返回给前端，
     * 因为可能会暴露数据库、服务器或代码细节。
     * 当前写法适合开发调试阶段使用。
     * </p>
     *
     * @param e 未知异常对象
     * @return 统一错误响应，HTTP 状态码为 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        "INTERNAL_ERROR",
                        "Internal server error.",
                        e.getMessage()
                ));
    }
}