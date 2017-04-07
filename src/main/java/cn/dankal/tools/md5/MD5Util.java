package cn.dankal.tools.md5;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * md5加密工具类
 */
public class MD5Util {

    private final static int count = 2; // 默认加密层数

    /**
     * md5加密
     *
     * @param string 明文
     * @return 密文
     */
    public static String md5(String string) {
        for (int i = 0; i < count; i++) {
            string = md5Encrypt(string);
        }
        return string;
    }

    /**
     * md5加密
     *
     * @param string 明文
     * @param count  加密次数
     * @return 密文
     */
    public static String md5(String string, int count) {
        for (int i = 0; i < count; i++) {
            string = md5Encrypt(string);
        }
        return string;
    }

    private static String md5Encrypt(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16).toUpperCase();
        } catch (Exception e) {
            System.out.print(e.getLocalizedMessage());
            return null;
        }
    }
}