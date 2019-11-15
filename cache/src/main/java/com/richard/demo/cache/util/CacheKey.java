package com.richard.demo.cache.util;

import org.springframework.util.Assert;

public class CacheKey implements java.io.Serializable {
    private static final long serialVersionUID = 7961687309716471657L;
    private String value = "0";

    public static CacheKey of(Object... params) {
        CacheKey cacheKey = new CacheKey();
        Assert.notNull(params, "");

        if (params.length == 1) {
            Object o = params[0];
            if (o instanceof CacheKey) {
                cacheKey.value = ((CacheKey) o).value;
            }
        }

        return cacheKey;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
