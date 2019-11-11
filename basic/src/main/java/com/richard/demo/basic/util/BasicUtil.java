package com.richard.demo.basic.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BasicUtil {

    @SafeVarargs
    public static <E> Set<E> setOf(E... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }
}
