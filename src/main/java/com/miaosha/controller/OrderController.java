package com.miaosha.controller;

import com.miaosha.pojo.MiaoshaUser;
import com.miaosha.pojo.OrderInfo;
import com.miaosha.redis.RedisService;
import com.miaosha.result.CodeMsg;
import com.miaosha.result.Result;
import com.miaosha.service.GoodsService;
import com.miaosha.service.MiaoShaUserService;
import com.miaosha.service.OrderService;
import com.miaosha.utils.CheckObjectIsNull;
import com.miaosha.vo.GoodsVo;
import com.miaosha.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private MiaoShaUserService userService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private GoodsService goodsService;

    @RequestMapping("detail")
    @ResponseBody
    public Result<OrderDetailVo> info(Model model, MiaoshaUser user,
                                      @RequestParam("orderId")long orderId){
        if(CheckObjectIsNull.objCheckIsNull(user)){
            return Result.error(CodeMsg.MOBILE_EMPTY);
        }
        OrderInfo orderInfo = orderService.getOrderById(orderId);
        if (orderInfo == null) {
            return Result.error(CodeMsg.ORDER_INFO_EMPTY);
        }
        long goodsId = orderInfo.getGoodsId();
        GoodsVo goodsVo = goodsService.queryGoodsVoByGoodsId(goodsId);
        OrderDetailVo vo = new OrderDetailVo();
        vo.setGoodsVo(goodsVo);
        vo.setOrderInfo(orderInfo);

        return Result.success(vo);
    }

}
