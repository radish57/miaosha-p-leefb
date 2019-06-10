package com.miaosha.controller;

import com.miaosha.pojo.MiaoshaUser;
import com.miaosha.pojo.OrderInfo;
import com.miaosha.redis.GoodsKey;
import com.miaosha.redis.RedisService;
import com.miaosha.result.Result;
import com.miaosha.service.GoodsService;
import com.miaosha.service.MiaoShaUserService;
import com.miaosha.vo.GoodsDetailVo;
import com.miaosha.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.context.webflux.SpringWebFluxContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.util.StringUtils;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {
    private static Logger log = LoggerFactory.getLogger(GoodsController.class);

    @Autowired
    private RedisService redisService;


    @Autowired
    private GoodsService goodsService;

    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    /**
     * 页面缓存
     * @param request
     * @param response
     * @param miaoshaUser
     * @param model
     * @return
     */
    @RequestMapping(value = "/to_list",produces = "text/html")
    @ResponseBody
    public String toLogin(HttpServletRequest request, HttpServletResponse response,
                          MiaoshaUser miaoshaUser , Model model) {
        List<GoodsVo> goodsList = goodsService.queryListGoodsVo();
        model.addAttribute("goodsList",goodsList);
        model.addAttribute("user",miaoshaUser);
        //return "goods_list";
        //取缓存
        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        IWebContext ctx = new WebContext(request,response,request.getServletContext(),
                request.getLocale(), model.asMap());

        //手动渲染
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list",ctx);
        if(!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsList,"",html);
        }
        return html;
    }


    /**
     * URL缓存
     * @param request
     * @param response
     * @param user
     * @param model
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response,
                                        MiaoshaUser user, Model model, @PathVariable("goodsId")long goodsId){

        GoodsVo goodsVo = goodsService.queryGoodsVoByGoodsId(goodsId);

        //取秒杀时间
        long startTime = goodsVo.getStartDate().getTime();
        long endTime = goodsVo.getEndDate().getTime();
        long now = System.currentTimeMillis();
        int miaoshaStatus = 0;
        int remainSeconds = 0; //剩余秒数

        if(now < startTime){  //秒杀还没开始
            miaoshaStatus = 0;
            remainSeconds = (int) ((startTime - now)/1000);
        }else if(now > endTime){ //秒杀结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else {  //秒杀ing
            miaoshaStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("miaoshaStatus",miaoshaStatus);
        model.addAttribute("remainSeconds",remainSeconds);
        IWebContext ctx = new WebContext(request,response,request.getServletContext(),
                request.getLocale(), model.asMap());
        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setMiaoshaStatus(miaoshaStatus);
        vo.setRemainSeconds(remainSeconds);
        vo.setGoodsVo(goodsVo);
        vo.setUser(user);


        return Result.success(vo);

    }


}
