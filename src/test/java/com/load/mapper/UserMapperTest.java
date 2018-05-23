package com.load.mapper;

import com.load.BaseTest;
import com.load.entity.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class UserMapperTest extends BaseTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void list() {
        List<User> users = userMapper.list();
        for (User user : users) {
            System.out.println(user.toString());
        }
    }
}