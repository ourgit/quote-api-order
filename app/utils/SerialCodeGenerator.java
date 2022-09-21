package utils;

/**
 * Created by win7 on 2016/7/4.
 */

import java.util.Random;

/**
 * 序列号生成器
 *
 * @author Johney
 */
public class SerialCodeGenerator {

    /**
     * 序列最小长度
     */
    public static final int CODE_LEN = 6;

    public static String makeSerialNumber() {
        String SALTCHARS = "1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < CODE_LEN) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }
    public static String makeSerialNumberByChar() {
        String SALTCHARS = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < CODE_LEN) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }
}
