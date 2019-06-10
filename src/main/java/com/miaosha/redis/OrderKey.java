package com.miaosha.redis;

public class OrderKey extends BasePrefix{
    public OrderKey(String prefix) {
        super(prefix);
    }

    public static OrderKey getMiaoshaOrderByUserIdGoodsId = new OrderKey("moug");
}
