package com.load.mapper;

import com.load.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

public interface UserMapper {

    List<User> list();
}
