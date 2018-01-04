package com.magic.microspider.util;

import java.util.Random;

/**
 * Created by liunn on 2018/1/4.
 */
public class RandomUtil {

    private static final String RANDOM_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String RANDOM_NUMBER = "0123456789";

    public static String randomString(int len) {
        if (len <= 0)
            return "";

        StringBuilder sb = new StringBuilder();
        Random radom = new Random();
        for (int r = 0; r < len; ++r) {
            int at = radom.nextInt(62);

            sb.append(RANDOM_STRING.charAt(at));
        }

        return sb.toString();
    }

    public static String randomNumber(int len) {
        if (len <= 0)
            return "";

        StringBuilder sb = new StringBuilder();
        Random radom = new Random();
        for (int r = 0; r < len; ++r) {
            int at = radom.nextInt(10);

            sb.append(RANDOM_NUMBER.charAt(at));
        }

        return sb.toString();
    }

    public static int randomInt(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    public static int randomInt() {
        return randomInt(0, 1);
    }
}
