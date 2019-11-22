package com.richard.demo.cache.util;

import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.Map;

public final class CacheExpireData {
    private final Map<String, Integer> cacheExpireMap = new LinkedHashMap<>();

    public void put(String cacheName, Integer expireSeconds) {
        Assert.notNull(cacheName, "Cache Name can't be empty!");
        Assert.notNull(expireSeconds, "Cache expire time can't be empty!");
        if (expireSeconds <= 0) {
            this.cacheExpireMap.put(cacheName, expireSeconds);
        } else {
            this.cacheExpireMap.put(cacheName, Math.max(expireSeconds, CacheTime.FIVE_MINUTES));
        }
    }

    Map<String, Long> toMilliSeconds() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry: cacheExpireMap.entrySet()) {
            map.put(entry.getKey(), Long.valueOf(entry.getValue()));
        }
        return map;
    }
}
