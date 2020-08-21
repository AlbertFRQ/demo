package com.richard.demo.basic.lock;

import com.richard.demo.basic.util.SpringContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class DistributedLock {

    boolean lock(String key, String owner, long expiredTime, long waitingTime) {
        long overdueTime = System.currentTimeMillis() + waitingTime;
        do {
            if (lock(key, owner, expiredTime)) {
                return true;
            }
        } while (overdueTime > System.currentTimeMillis());
        return false;
    }

    boolean lock(String key, String owner, long expiredTime) {
        String script = "return redis.call('set', KEYS[1], ARGV[1], 'PX', ARGV[2], 'NX')";
        RedisScript<String> redisScript = new DefaultRedisScript<>(script, String.class);
        String result = SpringContext.getBean(StringRedisTemplate.class).execute(redisScript, Collections.singletonList(key),
                owner, String.valueOf(expiredTime));
        return "OK".equals(result);
    }

    boolean unlock(String key, String owner) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long result = SpringContext.getBean(StringRedisTemplate.class).execute(redisScript, Collections.singletonList(key), owner);
        return result == 1;
    }

    boolean unlock(String key) {
        String script = "return redis.call('del', KEYS[1])";
        RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long result = SpringContext.getBean(StringRedisTemplate.class).execute(redisScript, Collections.singletonList(key));
        return result == 1;
    }
}
