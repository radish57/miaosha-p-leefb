package com.miaosha.service;

import com.miaosha.exception.GlobalException;
import com.miaosha.mapper.GoodsMapper;
import com.miaosha.pojo.Goods;
import com.miaosha.pojo.MiaoshaGoods;
import com.miaosha.pojo.OrderInfo;
import com.miaosha.result.CodeMsg;
import com.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;


    public List<GoodsVo> queryListGoodsVo(){
        return goodsMapper.queryListGoodsVo();
    }

    public GoodsVo queryGoodsVoByGoodsId(long goodsId) {

        GoodsVo goodsVo = goodsMapper.queryGoodsDetail(goodsId);
        if(goodsVo == null){
            throw new GlobalException(CodeMsg.ORDER_INFO_EMPTY);
        }
        return goodsVo;
    }

    public boolean reduceStock(GoodsVo goods) {
        MiaoshaGoods g = new MiaoshaGoods();
        g.setGoodsId(goods.getId());
        int ret =goodsMapper.reduceStock(g);
        return ret > 0;
    }
}
