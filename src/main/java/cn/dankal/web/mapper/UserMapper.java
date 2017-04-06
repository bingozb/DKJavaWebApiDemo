package cn.dankal.web.mapper;

import cn.dankal.web.model.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper {

    @Select("select * from user where username=#{username}")
    User selectUserByUsername(@Param("username") String username);

    @Select("select * from user")
    List<User> selectAllUser();
}