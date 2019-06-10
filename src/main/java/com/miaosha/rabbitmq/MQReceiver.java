package com.miaosha.rabbitmq;

import com.miaosha.pojo.MiaoshaOrder;
import com.miaosha.pojo.MiaoshaUser;
import com.miaosha.pojo.OrderInfo;
import com.miaosha.redis.RedisService;
import com.miaosha.result.CodeMsg;
import com.miaosha.result.Result;
import com.miaosha.service.GoodsService;
import com.miaosha.service.MiaoshaService;
import com.miaosha.service.OrderService;
import com.miaosha.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {
    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MiaoshaService miaoshaService;


    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

    /*@RabbitListener(queues = MQConfig.QUEUE)
    public void receive(String message){
        logger.info("receive message:"+message);

    }*/
    @RabbitListener(queues=MQConfig.MIAOSHA_QUEUE)
    public void receive(String message){
        log.info("receive message:"+message);
        MiaoshaMessage miaoshaMessage = RedisService.stringToBean(message, MiaoshaMessage.class);
        MiaoshaUser user = miaoshaMessage.getUser();
        long goodsId = miaoshaMessage.getGoodsId();
        //判断库存
        GoodsVo goods = goodsService.queryGoodsVoByGoodsId(goodsId);
        int stock = goods.getGoodsStock();
        if(stock <= 0){
            return ;
        }
        //判断曾是否秒杀成功
        MiaoshaOrder miaoshaOrder = orderService.getMiaoshaOrderByUserIdAndGoodsId(user.getId(),goodsId);
        if(miaoshaOrder != null){
            return ;
        }
        //减库存，下单，写入秒杀订单
        miaoshaService.miaosha(user,goods);

    }
}
