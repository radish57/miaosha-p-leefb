package com.miaosha.service;

import com.miaosha.pojo.Goods;
import com.miaosha.pojo.MiaoshaOrder;
import com.miaosha.pojo.MiaoshaUser;
import com.miaosha.pojo.OrderInfo;
import com.miaosha.redis.MiaoshaKey;
import com.miaosha.redis.RedisService;
import com.miaosha.utils.CheckObjectIsNull;
import com.miaosha.utils.MD5Utils;
import com.miaosha.utils.UUIDUtils;
import com.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Service
public class MiaoshaService {
    private static char[] ops = new char[]{'+','-','*'};

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisService redisService;

    @Transactional
    public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
        //减库存 下订单 写入秒杀订单
        boolean success = goodsService.reduceStock(goods);
        if(success) {
            return orderService.createOrder(user,goods);
        }else {
            //标记
            setGoodsOver(goods.getId());
            return null;
        }

    }

    private void setGoodsOver(long goodsId) {
        redisService.set(MiaoshaKey.isGoodsOver,""+goodsId,true);
    }


    public long getMiaoshaResult(long id, long goodsId) {
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdAndGoodsId(id, goodsId);
        if(order != null){
            return order.getOrderId();
        }else {
            boolean isOver = getGoodsOver(goodsId);
            if(isOver){
                return -1;
            }else {
                return 0;
            }
        }
    }

    private boolean getGoodsOver(long goodsId) {
        return redisService.exists(MiaoshaKey.isGoodsOver,""+goodsId);
    }



    public String createMiaoshaPath(MiaoshaUser user,long goodsId) {
        String str = MD5Utils.md5(UUIDUtils.uuid()+"123456");
        redisService.set(MiaoshaKey.getMiaoshaPath,""+String.valueOf(user.getId())+"_"+goodsId,str);
        return str;
    }

    public boolean checkOldPath(String path, MiaoshaUser user, long goodsId) {
        if(user == null || path == null){
            return false;
        }
        String oldPath = redisService.get(MiaoshaKey.getMiaoshaPath, "" + user.getId() + "_" + goodsId, String.class);
        return path.equals(oldPath);
    }

    /**
     * 生成验证码
     * @param user
     * @param goodsId
     * @return
     */
    public BufferedImage createMiaoshaVerifyCode(MiaoshaUser user, long goodsId) {
        if(user == null || goodsId <= 0){
            return null;
        }
        int width = 80;
        int height = 32;
        //生成验证码图片
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        //设置背景颜色
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        //编制边框
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // 自动随机生成验证码
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //把验证码存到redis中
        int rnd = calc(verifyCode);
        redisService.set(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, rnd);
        //输出图片
        return image;

    }
    //生成验证码数值脚本
    private int calc(String exp) {
        try{
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer)engine.eval(exp);

        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }


    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = ""+num1+op1+num2+op2+num3;
        return exp;
    }

    /**
     * 校验验证码
     * @param user
     * @param goodsId
     * @param verifyCode
     * @return
     */
    public boolean checkVerifyCode(MiaoshaUser user, long goodsId, int verifyCode) {
        if(user == null || goodsId <= 0){
            return false;
        }
        Integer code = redisService.get(MiaoshaKey.getMiaoshaVerifyCode, user.getId() + "," + goodsId, Integer.class);
        if(code == null || code - verifyCode != 0){
            return false;
        }
        //删除验证码缓存
        redisService.delete(MiaoshaKey.getMiaoshaVerifyCode, user.getId() + "," + goodsId);
        return true;
    }
}
