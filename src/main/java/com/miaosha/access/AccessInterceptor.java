package com.miaosha.access;

import com.alibaba.fastjson.JSON;
import com.miaosha.pojo.MiaoshaUser;
import com.miaosha.redis.AccessKey;
import com.miaosha.redis.RedisService;
import com.miaosha.result.CodeMsg;
import com.miaosha.result.Result;
import com.miaosha.service.MiaoShaUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.thymeleaf.util.StringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private MiaoShaUserService miaoShaUserService;
    @Autowired
    private RedisService redisService;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if(handler instanceof HandlerMethod){
            MiaoshaUser user = getUser(request, response);
            //把用户放到当前线程
            UserContext.setUser(user);

            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if(accessLimit == null){
                return true;
            }
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();

            String key = request.getRequestURI();
            if(needLogin){
                if(user == null){
                    render(response,CodeMsg.SESSION_ERROR);
                    return false;
                }
                key += "_"+user.getId();
            }else {

            }
            //martine flower,重构，改善既有代码的设计
            AccessKey ak = AccessKey.withExpire(seconds);
            Integer count = redisService.get(ak, key, Integer.class);
            if(count == null){
                redisService.set(ak, key,1);
            }else if(count < maxCount){
                redisService.incr(ak, key);
            }else {
                render(response,CodeMsg.ACCESS_LIMIT);
                return false;
            }
        }
        return super.preHandle(request, response, handler);
    }

    private void render(HttpServletResponse response,CodeMsg codeMsg) throws Exception{
        response.setContentType("application/json;charset=UTF-8");
        OutputStream outputStream = response.getOutputStream();
        String str = JSON.toJSONString(Result.error(codeMsg));
        outputStream.write(str.getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();
    }

    private MiaoshaUser getUser(HttpServletRequest request,HttpServletResponse response){
        String paramToken = request.getParameter("token");
        String cookieToken = getCookieValue(request,"token");
        //判断cookie和session是否为空,都为空，则返回至登陆页面
        if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        return miaoShaUserService.getByToken(response,token);
    }

    private String getCookieValue(HttpServletRequest request, String cookieNameToken) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null || cookies.length<=0){
            return null;
        }

        for (Cookie cookie : cookies){
            if(cookie.getName().equals(cookieNameToken)){
                return cookie.getValue();
            }
        }
        return null;
    }
}
