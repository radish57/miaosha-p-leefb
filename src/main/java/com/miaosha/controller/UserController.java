package com.miaosha.controller;

import com.miaosha.pojo.MiaoshaUser;
import com.miaosha.redis.RedisService;
import com.miaosha.result.Result;
import com.miaosha.service.MiaoShaUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private MiaoShaUserService userService;

    @Autowired
    private RedisService redisService;
    //QPS:849.5
    @RequestMapping("/info")
    @ResponseBody
    public Result<MiaoshaUser> info(Model model,MiaoshaUser user){
        return Result.success(user);
    }
}
