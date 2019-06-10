package com.miaosha.utils;


import org.apache.commons.codec.digest.DigestUtils;

public class MD5Utils {
    public static String md5(String src){
        return DigestUtils.md5Hex(src);
    }

    private static final String salt = "1a2b3c4d";

    public static String inputPassFormPass(String inputPass){
        String str = ""+salt.charAt(0)+salt.charAt(2)+inputPass+salt.charAt(5)+salt.charAt(4);
        return md5(str);
    }

    public static String formPassToDBPass(String formPass,String salt){
        String str = ""+salt.charAt(0)+salt.charAt(2)+formPass+salt.charAt(5)+salt.charAt(4);
        return md5(str);
    }
                        //inputPassToDbPass
    public static String inputPassToDBPass(String input,String saltDB){
        String formPass = inputPassFormPass(input);
        String DBPass = formPassToDBPass(formPass,saltDB);
        return DBPass;
    }



    public static void main(String[] args) {
        /*System.out.println(inputPassFormPass("123456"));
        System.out.println(formPassToDBPass(inputPassFormPass("123456"),"12gr1rfs3"));*/
        System.out.println(inputPassToDBPass("123456","1a2b3c4d"));
    }

}
