package com.richard.demo.cache;

import com.richard.demo.cache.util.CacheExpireConfigProcessor;
import com.richard.demo.cache.util.CacheExpireWriter;
import com.richard.demo.cache.util.CacheKey;
import com.richard.demo.cache.util.CacheTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.List;

@Slf4j
@Configuration
@EnableCaching
@Import({RedisAutoConfiguration.class})
public class CacheConfiguration extends CachingConfigurerSupport implements EnvironmentAware {

    private Environment environment;

    @Primary
    @Bean(name = "redisTemplate")
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setEnableTransactionSupport(false);

        RedisSerializer keySerializer = new StringRedisSerializer();
        RedisSerializer valueSerializer = new JdkSerializationRedisSerializer();
        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setHashKeySerializer(keySerializer);
        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);
        return redisTemplate;
    }

    @Primary
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
        stringRedisTemplate.setEnableTransactionSupport(false);
        return stringRedisTemplate;
    }

    @Primary
    @Bean
    public CacheExpireWriter cacheExpireWriter(List<CacheExpireConfigProcessor> list, RedisConnectionFactory redisConnectionFactory) {
        return new CacheExpireWriter(list, redisConnectionFactory, environment);
    }

    @Primary
    @Bean
    public CacheManager cacheManager(RedisCacheWriter cacheWriter, CacheKeyPrefix cacheKeyPrefix) {
        RedisSerializer keySerializer = new StringRedisSerializer();
        RedisSerializer valueSerializer = new JdkSerializationRedisSerializer();
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(CacheTime.ONE_DAY))
                .disableCachingNullValues()
                .computePrefixWith(cacheKeyPrefix)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer));
        return RedisCacheManager.builder(cacheWriter).cacheDefaults(redisCacheConfiguration).build();
    }

    @Bean
    public CacheKeyPrefix cacheKeyPrefix() {
        return cacheName -> {
            String uniquePrefix = environment.getProperty("richard.cache.prefix");
            if (uniquePrefix != null) {
                return uniquePrefix.concat(":").concat(cacheName).concat(":");
            } else {
                return cacheName.concat(":");
            }
        };
    }

    @Override
    @Bean
    public KeyGenerator keyGenerator() {
        return ((target, method, params) -> CacheKey.of(params));
    }

    @Override
    @Bean
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn(String.format("Error occurred when getting cache, cache: %s, key: %s, error: %s",
                        cache.getName(), key, exception.getMessage()));
                throw exception;
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, @Nullable Object value) {
                log.warn(String.format("Error occurred when putting cache, cache: %s, key: %s, error: %s",
                        cache.getName(), key, exception.getMessage()));
                throw exception;
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn(String.format("Error occurred when evicting cache, cache: %s, key: %s, error: %s",
                        cache.getName(), key, exception.getMessage()));
                throw exception;
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn(String.format("Error occurred when clearing cache, cache: %s, error: %s",
                        cache.getName(), exception.getMessage()));
                throw exception;
            }
        };
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
