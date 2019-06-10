package com.miaosha.controller;

import com.miaosha.rabbitmq.MQSender;
import com.miaosha.redis.RedisService;
import com.miaosha.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SampleController {
    @Autowired
    private RedisService redisService;
    @Autowired
    private MQSender mqSender;

    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> mq(){
       // mqSender.send("Hello,Leef");
        return Result.success("Hello,world");
    }
}
