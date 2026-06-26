package com.farm2future.farm2future_backend;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.farm2future.farm2future_backend.model.user.entity.AppUser;
import com.farm2future.farm2future_backend.model.user.mapper.AppUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class UserSeedTest {
    @Autowired
    private AppUserMapper appUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createTestUsers() {
        createUserIfNotExists(
                "u1",
                "Joseph",
                "demo@farmer.com",
                "123456",
                "farmer",
                "Green Valley Farm"
        );

        createUserIfNotExists(
                "u2",
                "Regulator Admin",
                "demo@regulator.com",
                "123456",
                "regulator",
                "All Entities"
        );

        createUserIfNotExists(
                "u3",
                "System Admin",
                "demo@admin.com",
                "123456",
                "admin",
                "Farm2Future"
        );
    }

    private void createUserIfNotExists(
            String id,
            String name,
            String email,
            String rawPassword,
            String role,
            String entityName
    ) {
        AppUser existingUser = appUserMapper.selectOne(
                new LambdaQueryWrapper<AppUser>()
                        .eq(AppUser::getEmail, email)
                        .last("LIMIT 1")
        );

        if (existingUser != null) {
            System.out.println("User already exists: " + email);
            return;
        }

        AppUser user = new AppUser();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setEntityName(entityName);
        user.setDeleted(0);

        appUserMapper.insert(user);

        System.out.println("Created user: " + email + " / role: " + role);
    }
}
