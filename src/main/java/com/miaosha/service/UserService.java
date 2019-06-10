package com.miaosha.service;

import com.miaosha.mapper.UserMapper;
import com.miaosha.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    UserMapper userMapper;

    public User getById(int id){
        return userMapper.getById(id);
    }
}
