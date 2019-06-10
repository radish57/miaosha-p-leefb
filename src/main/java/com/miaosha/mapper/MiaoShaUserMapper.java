package com.miaosha.mapper;

import com.miaosha.pojo.MiaoshaUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MiaoShaUserMapper {
    @Select("select * from miaosha_user where id = #{id}")
    MiaoshaUser getById(@Param("id") long id);
    @Update("update miaosha_user set password = #{password} where id = #{id}")
    void updatePassword(MiaoshaUser toBeUpdate);
}
