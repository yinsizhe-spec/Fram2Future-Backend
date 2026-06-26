package com.farm2future.farm2future_backend.model.auth.controller;

import com.farm2future.farm2future_backend.model.auth.dto.LoginRequest;
import com.farm2future.farm2future_backend.model.auth.dto.LoginResponse;
import com.farm2future.farm2future_backend.model.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
