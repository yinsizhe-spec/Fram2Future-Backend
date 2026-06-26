package com.farm2future.farm2future_backend.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一错误响应类。
 *
 * <p>
 * 用于封装后端返回给前端的错误信息。
 * 当前响应结构符合接口文档中常见的错误格式：
 * </p>
 *
 * <pre>
 * {
 *   "error": {
 *     "code": "VALIDATION_ERROR",
 *     "message": "Request body failed validation.",
 *     "details": {}
 *   }
 * }
 * </pre>
 *
 * <p>
 * 通常由全局异常处理器 {@code GlobalExceptionHandler} 使用，
 * 不建议在 Controller 中手动频繁创建。
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    /**
     * 错误响应主体。
     *
     * <p>
     * 所有具体的错误信息都会放在 error 字段中，
     * 包括业务错误码、错误提示信息和详细错误内容。
     * </p>
     */
    private ErrorBody error;

    /**
     * 快速创建统一错误响应对象。
     *
     * <p>
     * 该方法用于简化错误响应的创建过程，
     * 方便全局异常处理器统一调用。
     * </p>
     *
     * @param code    业务错误码，例如 USER_NOT_FOUND、VALIDATION_ERROR
     * @param message 错误提示信息
     * @param details 错误详细信息，可以是 null、字符串、Map 或其他对象
     * @return 统一错误响应对象
     */
    public static ErrorResponse of(String code, String message, Object details) {
        return new ErrorResponse(new ErrorBody(code, message, details));
    }

    /**
     * 错误信息主体类。
     *
     * <p>
     * 该内部类用于表示 error 字段中的具体内容。
     * </p>
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorBody {

        /**
         * 业务错误码。
         *
         * <p>
         * 用于让前端或调用方识别具体错误类型。
         * 例如：
         * VALIDATION_ERROR、
         * UNAUTHORIZED、
         * FORBIDDEN、
         * INTERNAL_ERROR。
         * </p>
         */
        private String code;

        /**
         * 错误提示信息。
         *
         * <p>
         * 用于给前端展示或调试使用。
         * </p>
         */
        private String message;

        /**
         * 错误详细信息。
         *
         * <p>
         * 可以存放更具体的错误内容，例如：
         * 字段校验失败信息、异常原因、错误参数等。
         * 如果没有额外信息，可以为 null。
         * </p>
         */
        private Object details;
    }
}