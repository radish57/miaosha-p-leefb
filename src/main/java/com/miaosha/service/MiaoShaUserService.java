package com.miaosha.service;

import com.miaosha.exception.GlobalException;
import com.miaosha.mapper.MiaoShaUserMapper;
import com.miaosha.pojo.MiaoshaUser;
import com.miaosha.redis.MiaoshaUserKey;
import com.miaosha.redis.RedisService;
import com.miaosha.redis.UserKey;
import com.miaosha.result.CodeMsg;
import com.miaosha.utils.CheckObjectIsNull;
import com.miaosha.utils.MD5Utils;
import com.miaosha.utils.UUIDUtils;
import com.miaosha.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class MiaoShaUserService {


    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    private MiaoShaUserMapper miaoShaUserMapper;

    @Autowired
    private RedisService redisService;


    public MiaoshaUser getById(long id){
        //取缓存
        MiaoshaUser user = redisService.get(MiaoshaUserKey.getById,""+id,MiaoshaUser.class);
        if(user!=null && !CheckObjectIsNull.objCheckIsNull(user)){
            return user;
        }
        //查数据库
        user = miaoShaUserMapper.getById(id);
        if(user!=null && !CheckObjectIsNull.objCheckIsNull(user)){
            redisService.set(MiaoshaUserKey.getById,""+id,user);
        }
        return user;
    }

    public boolean login(HttpServletResponse response,LoginVo loginVo) {
        if(loginVo == null){
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        MiaoshaUser user = getById(Long.parseLong(mobile));
        //判断手机号
        if(user == null){
            throw new GlobalException(CodeMsg.MOBILE_EMPTY);
        }
        String dbPass = user.getPassword();
        String saltDB = user.getSalt();
        String inputPass = MD5Utils.formPassToDBPass(password, saltDB);
        if(!inputPass.equals(dbPass)){
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //生成cookie
        String token = UUIDUtils.uuid();
        addCookie(response,token,user);
        return true;
    }

    public boolean updatePassword(String token,long id,String passwordNew){
        MiaoshaUser user = getById(id);
        if(user==null||CheckObjectIsNull.objCheckIsNull(user)==true){
            throw new GlobalException(CodeMsg.MOBILE_EMPTY);
        }
        MiaoshaUser toBeUpdate = new MiaoshaUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Utils.formPassToDBPass(passwordNew,user.getSalt()));
        miaoShaUserMapper.updatePassword(toBeUpdate);
        //处理缓存
        redisService.delete(MiaoshaUserKey.getById,""+id);
        user.setPassword(toBeUpdate.getPassword());
        redisService.set(MiaoshaUserKey.token,token,user);
        return true;
    }

    /**
     * 根据token获取对象
     * @param response
     * @param token
     * @return
     */
    public MiaoshaUser getByToken(HttpServletResponse response,String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
        if(user != null){
            addCookie(response,token,user);
        }
        return user;
    }

    private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
        redisService.set(MiaoshaUserKey.token,token,user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN,token);
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
