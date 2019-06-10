package com.miaosha.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class CheckObjectIsNull {
    public static boolean objCheckIsNull(Object object){
        Class clazz = object.getClass();
        Field[] fields = clazz.getDeclaredFields();  //获取所有属性
        boolean flag = true;
        for(Field field : fields){
            field.setAccessible(true);
            Object fieldValue = null;
            try{
                fieldValue = field.get(object);

            }catch (IllegalAccessException e){
                e.printStackTrace();
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }
            if(fieldValue != null){
                flag = false;
                break;
            }

        }
        return flag;
    }
}
