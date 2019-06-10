package com.miaosha.test;

import com.miaosha.utils.CheckObjectIsNull;

public class User {
    public String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static void main(String[] args) {
        User user = new User();
        user.setName("小明");
        if(CheckObjectIsNull.objCheckIsNull(user)){
            System.out.println("对象为空");
        }else {
            System.out.println("对象不为空");
        }
    }
}
