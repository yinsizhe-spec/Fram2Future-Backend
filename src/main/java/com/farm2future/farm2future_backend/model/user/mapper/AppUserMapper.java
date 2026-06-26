package com.farm2future.farm2future_backend.model.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.farm2future.farm2future_backend.model.user.entity.AppUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppUserMapper extends BaseMapper<AppUser> {
}
