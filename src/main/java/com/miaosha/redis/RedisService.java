package com.miaosha.redis;

import com.alibaba.fastjson.JSON;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Edit by Leef
 * 2019.5.5
 */
@Service
public class RedisService {



    @Autowired
    JedisPool jedisPool;

    public <T> T get(KeyPrefix prefix,String key,Class<T> clazz){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realPrefix = prefix.getPrefix()+key;
            String str = jedis.get(realPrefix);
            T t = stringToBean(str,clazz);
            return t;
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * Set the key , expireSeconds and value.
     * @param prefix
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    public <T> Boolean set(KeyPrefix prefix,String key,T value){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String str = beanToString(value);
            if(str == null || str.length() < 0){
                return false;
            }
            //生成模块真正key
            String realKey = prefix.getPrefix()+key;
            int seconds = prefix.expireSeconds();  //设置过期时间
            if(seconds <= 0){
                jedis.set(realKey,str);
            }else {
                jedis.setex(realKey,seconds,str);
            }
            return true;
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * Judge the key if exists.
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> boolean exists(KeyPrefix prefix,String key){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成模块真正key
            String realKey = prefix.getPrefix()+key;

            return jedis.exists(realKey);
        }finally {
            returnToPool(jedis);
        }
    }
    /**
     * Judge the key if exists.
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> boolean delete(KeyPrefix prefix,String key){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成模块真正key
            String realKey = prefix.getPrefix()+key;
            long del = jedis.del(realKey);
            return del > 0;
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * Increment the number stored at key by one.
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> Long incr(KeyPrefix prefix,String key){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成模块真正key
            String realKey = prefix.getPrefix()+key;

            return jedis.incr(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * Decrement the number stored at key by one.
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> Long decr(KeyPrefix prefix,String key){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成模块真正key
            String realKey = prefix.getPrefix()+key;

            return jedis.decr(realKey);
        }finally {
            returnToPool(jedis);
        }
    }


    public static  <T> String beanToString(T value) {
        if(value == null){
            return null;
        }
        Class<?> clazz = value.getClass();
        if(clazz == int.class || clazz == Integer.class){
            return ""+value;
        }else if(clazz == String.class){
            return (String)value;
        }else if(clazz == long.class || clazz == Long.class){
            return ""+value;
        }else {
            return JSON.toJSONString(value);
        }
    }


    public static  <T> T stringToBean(String str,Class<T> clazz) {
        if(str == null || str.length() <= 0 || clazz == null){
            return null;
        }
        if(clazz == int.class || clazz == Integer.class){
            return (T) Integer.valueOf(str);
        }else if(clazz == String.class){
            return (T) str;
        }else if(clazz == long.class || clazz == Long.class){
            return (T) Long.valueOf(str);
        }else {
            return JSON.toJavaObject(JSON.parseObject(str),clazz);
        }
    }

    private void returnToPool(Jedis jedis) {
        if(jedis != null){
            jedis.close();
        }
    }


}
