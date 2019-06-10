package com.miaosha.controller;

import com.miaosha.access.AccessLimit;
import com.miaosha.pojo.MiaoshaOrder;
import com.miaosha.pojo.MiaoshaUser;
import com.miaosha.rabbitmq.MQSender;
import com.miaosha.rabbitmq.MiaoshaMessage;
import com.miaosha.redis.AccessKey;
import com.miaosha.redis.GoodsKey;
import com.miaosha.redis.MiaoshaKey;
import com.miaosha.redis.RedisService;
import com.miaosha.result.CodeMsg;
import com.miaosha.result.Result;
import com.miaosha.service.GoodsService;
import com.miaosha.service.MiaoshaService;
import com.miaosha.service.OrderService;
import com.miaosha.utils.CheckObjectIsNull;
import com.miaosha.utils.MD5Utils;
import com.miaosha.utils.UUIDUtils;
import com.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import sun.security.provider.MD5;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/miaosha")
public class MiaoShaController implements InitializingBean {
    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MiaoshaService miaoshaService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MQSender mqSender;

    private Map<Long,Boolean> localOverMap = new HashMap<>();

    /**
     * 系统初始化的时候完成这个
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsVos = goodsService.queryListGoodsVo();
        if (CollectionUtils.isEmpty(goodsVos)) {
            return;
        }
        for (GoodsVo vo : goodsVos){
            redisService.set(GoodsKey.getMiaoshaGoodsStock,""+vo.getId(),vo.getGoodsStock());
            localOverMap.put(vo.getId(),false); //本地标记，减少redis访问
        }
    }

   // @RequestMapping(value = "/{path}/do_miaosha",method = RequestMethod.POST)
    @RequestMapping(value = "/do_miaosha",method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> miaosha(Model model, MiaoshaUser user,
                                   @RequestParam("goodsId")long goodsId
                                   /*@PathVariable("path")String path*/){
        model.addAttribute("user", user);
        if(CheckObjectIsNull.objCheckIsNull(user)){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        /*boolean check = miaoshaService.checkOldPath(path,user,goodsId);
        if(!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }*/
        Boolean isOver = localOverMap.get(goodsId);
        if(isOver){
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        //接受到秒杀请求，首先欲减少库存
        long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock,""+goodsId);
        if(stock<0){
            localOverMap.put(goodsId,true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        //判断曾是否秒杀成功
        MiaoshaOrder miaoshaOrder = orderService.getMiaoshaOrderByUserIdAndGoodsId(user.getId(),goodsId);
        if(miaoshaOrder != null){
            return Result.error(CodeMsg.MIAO_SHA_REPEAT);
        }

        //入队
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setUser(user);
        mm.setGoodsId(goodsId);
        mqSender.sendMiaoshaMessage(mm);
        return Result.success(0);


       /* //判断库存
        GoodsVo goods = goodsService.queryGoodsVoByGoodsId(goodsId);
        int stock = goods.getGoodsStock();
        if(stock <= 0){
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断曾是否秒杀成功
        MiaoshaOrder miaoshaOrder = orderService.getMiaoshaOrderByUserIdAndGoodsId(user.getId(),goodsId);
        if(miaoshaOrder != null){
            return Result.error(CodeMsg.MIAO_SHA_REPEAT);
        }
        //减库存，下单，写入秒杀订单
        OrderInfo orderInfo = miaoshaService.miaosha(user,goods);

        return Result.success(orderInfo);*/



    }

    /**
     * 轮询秒杀结果：0 排队 ；orderId 成功；-1 失败
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/result",method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> result(Model model, MiaoshaUser user,
                                   @RequestParam("goodsId")long goodsId) {
        model.addAttribute("user", user);
        if (CheckObjectIsNull.objCheckIsNull(user)) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result = miaoshaService.getMiaoshaResult(user.getId(),goodsId);
        return Result.success(result);
    }

    /**
     * 获取抢购的地址
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/path",method = RequestMethod.GET)
    @ResponseBody
    @AccessLimit(seconds = 5,maxCount = 5,needLogin = true)
    public Result<String> getMiaoshaPath(HttpServletRequest request,Model model, MiaoshaUser user,
                                         @RequestParam("goodsId")long goodsId,
                                         //这个设置是为了测试快速点击限制访问
                                         @RequestParam(value = "verifyCode",defaultValue = "0")int verifyCode) {
        model.addAttribute("user", user);
        if (CheckObjectIsNull.objCheckIsNull(user)) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        //校验验证码
        /*boolean check = miaoshaService.checkVerifyCode(user,goodsId,verifyCode);
        if(!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }*/
        String path = miaoshaService.createMiaoshaPath(user,goodsId);

        return Result.success(path);
    }

    /**
     * 获取验证码
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/verifyCode",method = RequestMethod.GET)
    public Result<String> getVerifyCode(HttpServletResponse response,Model model, MiaoshaUser user,
                                        @RequestParam("goodsId")long goodsId){
        model.addAttribute("user",user);
        if (CheckObjectIsNull.objCheckIsNull(user)) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        BufferedImage image = miaoshaService.createMiaoshaVerifyCode(user,goodsId);
        try{
            OutputStream out = response.getOutputStream();
            ImageIO.write(image,"jpeg",out);
            out.flush();
            out.close();
            return null;
        }catch (Exception e){
            e.printStackTrace();
            return Result.error(CodeMsg.MIAO_SHA_FAIL);
        }
    }


}
