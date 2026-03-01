package com.family.app.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.app.entity.Food;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FoodRepository extends BaseMapper<Food> {
}

