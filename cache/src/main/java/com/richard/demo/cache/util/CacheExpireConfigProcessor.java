package com.richard.demo.cache.util;

@FunctionalInterface
public interface CacheExpireConfigProcessor {

    void putExpireData(CacheExpireData cacheExpireData);
}
