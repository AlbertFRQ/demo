package com.richard.demo.basic.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApplicationDistributedLock {

    private static final String DISTRIBUTED_LOCK = "distributedLock";

    @Autowired
    private DistributedLock distributedLock;

    @Autowired
    private DistributedReentrantLock reentrantLock;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.application.name}_${spring.application.instance_id:${random.uuid}}")
    private String applicationInstanceId;


    public boolean lock(String key, long expiredTime) {
        String prefixKey = getKeyPrefix(applicationName);
        return distributedLock.lock(prefixKey + key, applicationInstanceId, expiredTime);
    }

    public boolean renewLock(String key, long expiredTime) {
        String prefixKey = getKeyPrefix(applicationName);
        return reentrantLock.renewLock(prefixKey + key, applicationInstanceId, expiredTime);
    }

    public boolean unlock(String key) {
        String prefixKey = getKeyPrefix(applicationName);
        return distributedLock.unlock(prefixKey + key, applicationInstanceId);
    }

    public boolean forceUnlock(String key) {
        String prefixKey = getKeyPrefix(applicationName);
        return distributedLock.unlock(prefixKey + key);
    }

    private String getKeyPrefix(String applicationName) {
        applicationName = applicationName != null ? applicationName : "_default";
        return applicationName.concat(":").concat(DISTRIBUTED_LOCK).concat(":");
    }
}
