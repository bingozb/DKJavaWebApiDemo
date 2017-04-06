package cn.dankal.tools.md5;

import java.security.MessageDigest;

/**
 * Created by bingo on 2017/4/1.
 */
public class MD5Util {

    // 加密的重数
    private static int count = 2;

    public static String MD5(String string) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        try {
            byte[] btInput = string.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            String password = new String(str);
            count--;
            if (count > 0)
                return MD5(password);
            else {
                count = 2;
                return password;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}