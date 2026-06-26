package com.farm2future.farm2future_backend.model.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.farm2future.farm2future_backend.model.user.entity.AppUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 Mapper 接口。
 *
 * <p>
 * 该接口用于操作数据库中的 app_user 表。
 * MyBatis-Plus 会根据 {@link BaseMapper} 自动提供常用的 CRUD 方法，
 * 因此这里暂时不需要手动编写 SQL。
 * </p>
 *
 * <p>
 * 常用方法示例：
 * </p>
 *
 * <pre>
 * appUserMapper.selectById(id);
 * appUserMapper.selectOne(queryWrapper);
 * appUserMapper.insert(user);
 * appUserMapper.updateById(user);
 * appUserMapper.deleteById(id);
 * </pre>
 *
 * <p>
 * 当前登录功能中，主要使用该 Mapper 根据邮箱、角色和删除状态查询用户。
 * </p>
 */
@Mapper
public interface AppUserMapper extends BaseMapper<AppUser> {
}