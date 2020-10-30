package com.richard.demo.basic.util;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

public class StringUtil {

    public static String shorten(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        maxLength = Math.max(maxLength, 10);
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }

    public static String toHashString(String str) {
        return hash(str) + "";
    }

    public static long hash(String str) {
        return Hashing.murmur3_32().hashString(str, StandardCharsets.UTF_8).asLong();
    }
}
