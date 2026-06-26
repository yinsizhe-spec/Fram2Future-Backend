package com.farm2future.farm2future_backend.model.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.farm2future.farm2future_backend.common.exception.BusinessException;
import com.farm2future.farm2future_backend.common.security.JwtTokenProvider;
import com.farm2future.farm2future_backend.model.auth.dto.LoginRequest;
import com.farm2future.farm2future_backend.model.auth.dto.LoginResponse;
import com.farm2future.farm2future_backend.model.user.entity.AppUser;
import com.farm2future.farm2future_backend.model.user.mapper.AppUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AppUserMapper appUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String role = request.getRole().trim().toLowerCase();

        validateRole(role);

        AppUser user = appUserMapper.selectOne(
                new LambdaQueryWrapper<AppUser>()
                        .eq(AppUser::getEmail, email)
                        .eq(AppUser::getRole, role)
                        .eq(AppUser::getDeleted, 0)
                        .last("LIMIT 1")
        );

        if (user == null) {
            throwInvalidCredentials();
        }

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!passwordMatches) {
            throwInvalidCredentials();
        }

        String token = jwtTokenProvider.generateToken(user);

        LoginResponse.UserProfile profile = new LoginResponse.UserProfile(
                user.getId(),
                user.getName(),
                user.getRole(),
                user.getEntityName()
        );

        return new LoginResponse(profile, token);
    }

    private void validateRole(String role) {
        if (!StringUtils.hasText(role)) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "VALIDATION_ERROR",
                    "Role is required."
            );
        }

        if (!role.equals("farmer") && !role.equals("regulator") && !role.equals("admin")) {
            throw new BusinessException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "VALIDATION_ERROR",
                    "Role must be farmer, regulator, or admin."
            );
        }
    }

    private void throwInvalidCredentials() {
        throw new BusinessException(
                HttpStatus.UNAUTHORIZED,
                "INVALID_CREDENTIALS",
                "Login failed. Please check your email, password, and role."
        );
    }
}
