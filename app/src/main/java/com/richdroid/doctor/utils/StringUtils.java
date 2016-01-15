package com.richdroid.doctor.utils;

/**
 * Created by richa.khanna on 8/8/15.
 */

public class StringUtils {

    private static final String EMPTY = "";
    private static final String NULL = "null";

    public static boolean isEmpty(String s) {
        return null == s || EMPTY.equals(s.trim());
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }
}
