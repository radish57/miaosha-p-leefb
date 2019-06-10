package com.miaosha.service;

import com.miaosha.constant.OrderInfoStatus;
import com.miaosha.mapper.OrderMapper;
import com.miaosha.pojo.MiaoshaOrder;
import com.miaosha.pojo.MiaoshaUser;
import com.miaosha.pojo.OrderInfo;
import com.miaosha.redis.OrderKey;
import com.miaosha.redis.RedisService;
import com.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisService redisService;

    public MiaoshaOrder getMiaoshaOrderByUserIdAndGoodsId(long userId, long goodsId) {
        //return orderMapper.getMiaoshaOrderByUserIdAndGoodsId(userId,goodsId);
        return redisService.get(OrderKey.getMiaoshaOrderByUserIdGoodsId,""+userId+"_"+goodsId,MiaoshaOrder.class);

    }

    @Transactional
    public OrderInfo createOrder(MiaoshaUser user, GoodsVo goods) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getMiaoshaPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(OrderInfoStatus.NOT_PAY.getStatus());
        orderInfo.setUserId(user.getId());

        orderMapper.insertOrderInfo(orderInfo); //不能通过返回值获取id  会有bug
        MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
        miaoshaOrder.setGoodsId(goods.getId());
        miaoshaOrder.setOrderId(orderInfo.getId());
        miaoshaOrder.setUserId(user.getId());
        orderMapper.insertMiaoshaOrder(miaoshaOrder);

        redisService.set(OrderKey.getMiaoshaOrderByUserIdGoodsId,""+user.getId()+"_"+goods.getId(),miaoshaOrder);

        return orderInfo;
    }

    public OrderInfo getOrderById(long orderId) {
        return orderMapper.getOrderById(orderId);
    }
}
