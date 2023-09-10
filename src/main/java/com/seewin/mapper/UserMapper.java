package com.seewin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seewin.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select(value = "select * from user where username=#{username}")
    public User selectByUsername(@Param("username") String username);
}
