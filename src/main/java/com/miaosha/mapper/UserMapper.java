package com.miaosha.mapper;

import com.miaosha.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("select * from miaosha_user where id = #{id}")
    User getById(@Param("id") int id);
    @Insert("insert into user (name) values (#{name})")
    User insert(User user);

}
