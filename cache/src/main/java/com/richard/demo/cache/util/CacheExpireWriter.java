package com.richard.demo.cache.util;

import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CacheExpireWriter implements RedisCacheWriter {

    private Map<String, Long> expires;
    private RedisCacheWriter cacheWriter;
    private Environment environment;

    public CacheExpireWriter(List<CacheExpireConfigProcessor> configProcessorList, RedisConnectionFactory redisConnectionFactory,
                             Environment environment) {
        this.expires = new LinkedHashMap<>();
        configProcessorList.forEach(c -> {
            CacheExpireData data = new CacheExpireData();
            c.putExpireData(data);
            this.expires.putAll(data.toMilliSeconds());
        });
        this.cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory);
        this.environment = environment;
    }

    private Duration getCacheExpireTime(String cacheName, Duration defaultTtl) {
        String ttlSecond = environment.getProperty("richard.cache.ttlSeconds");
        long ttl = Long.valueOf(ttlSecond != null ? ttlSecond : "0");
        if (ttl < CacheTime.MIN) {
            ttl = expires.getOrDefault(cacheName, ttl);
        }
        return Duration.ofSeconds(ttl);
    }

    @Override
    public void put(String name, byte[] key, byte[] value, Duration ttl) {
        cacheWriter.put(name, key, value, getCacheExpireTime(name, ttl));
    }

    @Override
    public byte[] get(String name, byte[] key) {
        return cacheWriter.get(name, key);
    }

    @Override
    public byte[] putIfAbsent(String name, byte[] key, byte[] value, Duration ttl) {
        return cacheWriter.putIfAbsent(name, key, value, getCacheExpireTime(name, ttl));
    }

    @Override
    public void remove(String name, byte[] key) {
        cacheWriter.remove(name, key);
    }

    @Override
    public void clean(String name, byte[] pattern) {
        cacheWriter.clean(name, pattern);
    }
}
