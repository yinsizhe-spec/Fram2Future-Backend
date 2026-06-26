package com.farm2future.farm2future_backend.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private ErrorBody error;

    public static ErrorResponse of(String code, String message, Object details) {
        return new ErrorResponse(new ErrorBody(code, message, details));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorBody {
        private String code;
        private String message;
        private Object details;
    }
}
