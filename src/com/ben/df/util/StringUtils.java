package com.ben.df.util;

/**
 * @author : wubenhao
 * @date : create in 2022/9/9
 */
public class StringUtils {

    public static String substringFront(String target, String args) {
        return target.substring(0,target.indexOf(args));
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static String subDataBaseByUrl(String url) {
        if (url.contains("?")) {
            return url.substring(url.lastIndexOf("/")+1,url.indexOf("?"));
        }
        return url.substring(url.lastIndexOf("/")+1);
    }

    public static String substring(String str, String startStr, String endStr) {
        return str.substring(str.lastIndexOf(startStr)+startStr.length(),str.lastIndexOf(endStr));
    }
}
