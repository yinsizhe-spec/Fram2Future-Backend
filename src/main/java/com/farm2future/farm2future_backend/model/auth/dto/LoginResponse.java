package com.farm2future.farm2future_backend.model.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private UserProfile user;

    private String token;

    @Data
    @AllArgsConstructor
    public static class UserProfile {
        private String id;
        private String name;
        private String role;
        private String entityName;
    }
}
