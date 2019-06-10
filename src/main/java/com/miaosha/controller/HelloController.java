package com.miaosha.controller;


import com.miaosha.pojo.User;
import com.miaosha.redis.RedisService;
import com.miaosha.redis.UserKey;
import com.miaosha.result.Result;
import com.miaosha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class HelloController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;


    @RequestMapping("/index")
    public ModelAndView hello(){
        ModelAndView mv = new ModelAndView();
        mv.setViewName("hello");
        mv.addObject("name","hello_miaosha");
        return mv;
    }

    @RequestMapping("/db/get")
    @ResponseBody
    public Result<User> dbGet(){
        User user = userService.getById(1);
        return Result.success(user);
    }

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet(){
        User user = redisService.get(UserKey.getById, "" + 1, User.class);
        return Result.success(user);
    }


    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet(){
        User user = new User();
        user.setId(1);
        user.setName("1123");
        redisService.set(UserKey.getById,""+1,user);
        return Result.success(true);
    }


}
