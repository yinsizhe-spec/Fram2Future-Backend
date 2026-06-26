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
 * <p>
 * 该测试类用于向数据库 app_user 表中写入开发环境测试账号。
 * 主要用于本地开发、接口联调、前端登录测试等场景。
 * </p>
 *
 * <p>
 * 注意：
 * 这里虽然写了明文密码 "123456"，
 * 但它不会直接保存到数据库。
 * 程序会通过 {@link PasswordEncoder} 自动生成 BCrypt 加密后的密码哈希，
 * 然后再写入数据库。
 * </p>
 *
 * <p>
 * 执行方式：
 * 在 IDE 中右键 createTestUsers 方法，选择 Run 即可。
 * </p>
 */
@SpringBootTest
public class UserSeedTest {

    /**
     * 用户表 Mapper。
     *
     * <p>
     * 用于向 app_user 表中查询和插入用户数据。
     * </p>
     */
    @Autowired
    private AppUserMapper appUserMapper;

    /**
     * 密码加密器。
     *
     * <p>
     * 用于把测试账号的明文密码转换成 BCrypt 密码哈希。
     * </p>
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 创建开发环境测试用户。
     *
     * <p>
     * 当前会创建三个测试账号：
     * </p>
     *
     * <ul>
     *     <li>farmer：农场用户</li>
     *     <li>regulator：监管用户</li>
     *     <li>admin：系统管理员</li>
     * </ul>
     *
     * <p>
     * 如果邮箱已经存在，则不会重复创建。
     * </p>
     */
//    @Test
    void createTestUsers() {
        // 创建农场用户测试账号
        createUserIfNotExists(
                "u1",
                "Joseph",
                "demo@farmer.com",
                "123456",
                "farmer",
                "Green Valley Farm"
        );

        // 创建监管用户测试账号
        createUserIfNotExists(
                "u2",
                "buyer",
                "demo@buyer.com",
                "123456",
                "buyer",
                "All Entities"
        );

        // 创建系统管理员测试账号
        createUserIfNotExists(
                "u3",
                "regulator",
                "demo@regulator.com",
                "123456",
                "regulator",
                "Farm2Future"
        );
    }

    /**
     * 如果用户不存在，则创建用户。
     *
     * <p>
     * 该方法会先根据邮箱查询数据库。
     * 如果邮箱已经存在，则直接跳过，避免重复插入测试用户。
     * </p>
     *
     * <p>
     * 如果用户不存在，则会创建新的 AppUser 对象，
     * 并将明文密码加密后保存到数据库。
     * </p>
     *
     * @param id          用户 ID
     * @param name        用户姓名
     * @param email       用户邮箱
     * @param rawPassword 明文密码，只用于生成 BCrypt 哈希，不会直接入库
     * @param role        用户角色，例如 farmer、regulator、admin
     * @param entityName  用户所属实体名称
     */
    private void createUserIfNotExists(
            String id,
            String name,
            String email,
            String rawPassword,
            String role,
            String entityName
    ) {
        /*
         * 根据邮箱查询用户是否已经存在。
         *
         * last("LIMIT 1") 表示只查询一条记录，
         * 避免数据库中异常存在重复邮箱时返回多条导致报错。
         */
        AppUser existingUser = appUserMapper.selectOne(
                new LambdaQueryWrapper<AppUser>()
                        .eq(AppUser::getEmail, email)
                        .last("LIMIT 1")
        );

        // 如果用户已经存在，则跳过创建
        if (existingUser != null) {
            System.out.println("User already exists: " + email);
            return;
        }

        // 创建新的用户实体对象
        AppUser user = new AppUser();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);

        /*
         * 对明文密码进行 BCrypt 加密。
         *
         * 数据库中保存的是加密后的密码哈希，
         * 不是 rawPassword 明文。
         */
        user.setPassword(passwordEncoder.encode(rawPassword));

        user.setRole(role);
        user.setEntityName(entityName);

        /*
         * 设置逻辑删除状态。
         *
         * 0 表示未删除，可以正常登录；
         * 1 表示已删除，登录逻辑中会禁止登录。
         */
        user.setDeleted(0);

        // 插入数据库
        appUserMapper.insert(user);

        // 在控制台输出创建结果，方便确认执行情况
        System.out.println("Created user: " + email + " / role: " + role);
    }
}