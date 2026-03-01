package com.family.app.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.app.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRepository extends BaseMapper<User> {
}
