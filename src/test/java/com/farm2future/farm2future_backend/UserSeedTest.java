package com.farm2future.farm2future_backend;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.farm2future.farm2future_backend.model.user.entity.AppUser;
import com.farm2future.farm2future_backend.model.user.mapper.AppUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 测试用户初始化类。
 *
 * 用于向 app_user 表中写入开发环境测试账号。
 * 密码会通过 PasswordEncoder 自动加密后保存。
 */
@SpringBootTest
public class UserSeedTest {

    @Autowired
    private AppUserMapper appUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

//    @Test
    void createTestUsers() {
        /*
         * 默认密码都是：123456
         *
         * 登录测试账号：
         * farmer: demo@farmer.com
         * buyer: demo@buyer.com
         * regulator: demo@regulator.com
         * admin: demo@admin.com
         */

        // ===============================
        // Farmer 用户
        // ===============================

        createUserIfNotExists(
                "u1",
                "Joseph",
                "demo@farmer.com",
                "123456",
                "farmer",
                "Green Valley Farm"
        );

        createUserIfNotExists(
                "u5",
                "Maya Farmer",
                "maya@farmer.com",
                "123456",
                "farmer",
                "Sunny Rice Farm"
        );

        createUserIfNotExists(
                "u6",
                "Chen Farmer",
                "chen@farmer.com",
                "123456",
                "farmer",
                "Blue River Farm"
        );

        createUserIfNotExists(
                "u7",
                "Nur Farmer",
                "nur@farmer.com",
                "123456",
                "farmer",
                "Highland Organic Farm"
        );

        createUserIfNotExists(
                "u8",
                "Ali Farmer",
                "ali@farmer.com",
                "123456",
                "farmer",
                "Palm Edge Farm"
        );

        createUserIfNotExists(
                "u9",
                "Wong Farmer",
                "wong@farmer.com",
                "123456",
                "farmer",
                "North Star Wheat Farm"
        );

        createUserIfNotExists(
                "u10",
                "Siti Farmer",
                "siti@farmer.com",
                "123456",
                "farmer",
                "Riverbend Corn Farm"
        );

        createUserIfNotExists(
                "u11",
                "Lim Farmer",
                "lim@farmer.com",
                "123456",
                "farmer",
                "Emerald Vegetable Farm"
        );

        createUserIfNotExists(
                "u12",
                "Hafiz Farmer",
                "hafiz@farmer.com",
                "123456",
                "farmer",
                "Golden Paddy Farm"
        );

        createUserIfNotExists(
                "u13",
                "Aina Farmer",
                "aina@farmer.com",
                "123456",
                "farmer",
                "Tropical Fruit Farm"
        );

        // ===============================
        // Buyer 用户
        // ===============================

        createUserIfNotExists(
                "u2",
                "Buyer Demo",
                "demo@buyer.com",
                "123456",
                "buyer",
                "AgroTrade Buyer Ltd"
        );

        createUserIfNotExists(
                "u14",
                "Tan Buyer",
                "tan@buyer.com",
                "123456",
                "buyer",
                "FreshMart Malaysia"
        );

        createUserIfNotExists(
                "u15",
                "Eco Buyer",
                "eco@buyer.com",
                "123456",
                "buyer",
                "EcoFood Distributor"
        );

        createUserIfNotExists(
                "u16",
                "Urban Market Buyer",
                "urban@buyer.com",
                "123456",
                "buyer",
                "Urban Market Hub"
        );

        // ===============================
        // Regulator 用户
        // ===============================

        createUserIfNotExists(
                "u3",
                "Regulator Demo",
                "demo@regulator.com",
                "123456",
                "regulator",
                "Farm2Future"
        );

        createUserIfNotExists(
                "u17",
                "Malaysia Food Regulator",
                "food.regulator@example.com",
                "123456",
                "regulator",
                "Malaysia Food Regulator"
        );

        createUserIfNotExists(
                "u18",
                "ESG Auditor",
                "esg.auditor@example.com",
                "123456",
                "regulator",
                "ESG Audit Department"
        );

        // ===============================
        // Admin 用户
        // ===============================

        createUserIfNotExists(
                "u4",
                "Admin Demo",
                "demo@admin.com",
                "123456",
                "admin",
                "Farm2Future Admin"
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