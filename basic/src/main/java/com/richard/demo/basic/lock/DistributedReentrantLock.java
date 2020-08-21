package com.richard.demo.basic.lock;

import com.richard.demo.basic.util.SpringContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class DistributedReentrantLock {

    boolean renewLock(String key, String owner, long expiredTime) {
        String script = "redis.call('setnx', KEYS[1], ARGV[1]); if(redis.call('get', KEYS[1])==ARGV[1]) then " +
                "redis.call('pexpire', KEYS[1], ARGV[2]); return 'OK'; end;";
        RedisScript<String> redisScript = new DefaultRedisScript<>(script, String.class);
        String result = SpringContext.getBean(StringRedisTemplate.class).execute(redisScript, Collections.singletonList(key),
                owner, String.valueOf(expiredTime));
        return "OK".equals(result);
    }
}
