package com.miaosha.mapper;

import com.miaosha.pojo.MiaoshaOrder;
import com.miaosha.pojo.OrderInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;

@Mapper
public interface OrderMapper {

    @Select("select * from miaosha_order where user_id = #{userId} and goods_id = #{goodsId}")
    MiaoshaOrder getMiaoshaOrderByUserIdAndGoodsId(long userId, long goodsId);

    @Insert("insert into order_info(user_id,goods_id,goods_name,goods_count,goods_price," +
            "order_channel,status,create_date) " +
            "values(#{userId},#{goodsId},#{goodsName},#{goodsCount},#{goodsPrice},#{orderChannel},#{status},#{createDate})")
    @SelectKey(keyColumn = "id",keyProperty = "id",resultType = long.class,before = false,statement = "select last_insert_id()")
    long insertOrderInfo(OrderInfo orderInfo);


    @Insert("insert into miaosha_order(user_id,order_id,goods_id) values(#{userId},#{orderId},#{goodsId})")
    void insertMiaoshaOrder(MiaoshaOrder miaoshaOrder);

    @Select("select * from miaosha_order where id = #{orderId}")
    OrderInfo getOrderById(long orderId);
}
