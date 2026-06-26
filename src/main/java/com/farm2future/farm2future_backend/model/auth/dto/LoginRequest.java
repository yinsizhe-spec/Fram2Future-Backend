package com.farm2future.farm2future_backend.model.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Email is required.")
    @Email(message = "Email format is invalid.")
    private String email;

    @NotBlank(message = "Password is required.")
    private String password;

    @NotBlank(message = "Role is required.")
    private String role;
}
