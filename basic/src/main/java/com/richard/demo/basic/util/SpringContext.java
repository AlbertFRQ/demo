package com.richard.demo.basic.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpringContext {

    private static volatile Cache<String, Object> cache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(2000).build();

    public static ApplicationContext getApplicationContext() {
        SpringContextConfiguration.assertInitialized();
        return SpringContextConfiguration.getApplicationContext();
    }

    public static Environment getEnvironment() {
        return getApplicationContext().getEnvironment();
    }

    public static <T> T getBean(Class<T> type) throws BeansException {
        return getBean(SpringContextConfiguration.getApplicationContext(), type);
    }

    public static <T> T getBean(ApplicationContext applicationContext, Class<T> type) throws BeansException {
        return applicationContext.getBean(type);
    }

    public static String getRequiredProperty(String prefix, String key, Environment environment) {
        String value = getProperty(prefix, key, environment);
        Assert.state(value != null, String.format("Required key %s is not found! DataSource: %s", key, prefix));
        return value;
    }

    public static String getProperty(String prefix, String key, Environment environment) {
        return getProperty(prefix, key, environment, null);
    }

    public static String getProperty(String prefix, String key, Environment environment, String defaultValue) {
        return (String) SpringContext.getProperty(environment, prefix + key, defaultValue);
    }

    public static Integer getIntProperty(String prefix, String key, Environment environment, int defaultValue) {
        return Integer.parseInt(getProperty(prefix, key, environment, String.valueOf(defaultValue)));
    }

    public static Boolean getBooleanProperty(String prefix, String key, Environment environment) {
        return "true".equalsIgnoreCase(getProperty(prefix, key, environment));
    }

    public static boolean isTrue(String key) {
        return isTrue(key, getEnvironment());
    }

    public static boolean isTrue(String key, Environment environment) {
        return getBooleanProperty(null, key, environment);
    }

    public static Set<String> getConfigGroups(Environment environment, String keyPrefix, String keySuffix) {
        Assert.state(StringUtils.hasText(keyPrefix), "keyPrefix required");
        Assert.state(StringUtils.hasText(keySuffix), "keySuffix required");

        String prefix = keyPrefix.endsWith(".") ? keyPrefix : (keyPrefix + ".");
        String suffix = keySuffix.startsWith(".") ? keySuffix : ("." + keySuffix);

        String cacheKey = SpringContext.class.getName() + "#getConfigGroups#" + prefix + "#" + suffix;
        Set<String> result = (Set<String>) getTransientProperty(cacheKey, environment);

        if (result == null) {
            result = new LinkedHashSet<>();
            String regEx = String.format("%s([^.]+)%s",
                    prefix.replaceAll("\\.", "\\\\."),
                    suffix.replaceAll("\\.", "\\\\."));
            Pattern pattern = Pattern.compile(regEx);
            for (PropertySource<?> propertySource : ((ConfigurableEnvironment) environment).getPropertySources()) {
                if (propertySource instanceof EnumerablePropertySource) {
                    for (String key : ((EnumerablePropertySource<?>) propertySource).getPropertyNames()) {
                        Matcher matcher = pattern.matcher(key);
                        if (matcher.find()) {
                            result.add(matcher.group(1));
                        }
                    }
                }
            }
            setTransientProperty(cacheKey, result);
        }

        return Collections.unmodifiableSet(result);
    }

    public static Map<String, String> getEnumerableProperties(Environment environment, String keyPrefix) {
        Assert.state(StringUtils.hasText(keyPrefix), "keyPrefix required");

        String prefix = keyPrefix.endsWith(".") ? keyPrefix : (keyPrefix + ".");
        String cacheKey = SpringContext.class.getName() + "#getEnumerableProperties#" + prefix;
        Map<String, String> result = (Map<String, String>) getTransientProperty(cacheKey, environment);

        if (result == null) {
            result = new LinkedHashMap<>();
            for (PropertySource<?> propertySource : ((ConfigurableEnvironment) environment).getPropertySources()) {
                if (propertySource instanceof EnumerablePropertySource) {
                    for (String key : ((EnumerablePropertySource<?>) propertySource).getPropertyNames()) {
                        if (key.startsWith(prefix)) {
                            String propKey = key.substring(prefix.length());
                            if (!result.containsKey(propKey)) {
                                result.put(propKey, environment.getProperty(key));
                            }
                        }
                    }
                }
            }
            setTransientProperty(cacheKey, result);
        }

        return Collections.unmodifiableMap(result);
    }

    public static Object getTransientProperty(String key) {
        return getTransientProperty(key, getEnvironment());
    }

    public static Object getTransientProperty(String key, Environment environment) {
        return getProperty(environment, key, null);
    }

    public static Object getProperty(Environment environment, String key, String defaultValue) {
        if (StringUtils.hasText(key)) {
            return null;
        }
        if (cache == null) {
            return environment.getProperty(key, defaultValue);
        }
        Object value = cache.getIfPresent(key);
        if (value == null) {
            value = ObjectUtil.value(environment.getProperty(key), NullObject.INSTANCE);
            cache.put(key, value);
        }
        return NullObject.INSTANCE == value ? defaultValue : value;
    }

    public static void setTransientProperty(String key, Object value) {
        if (cache != null) {
            cache.put(key, value);
        }
    }
}
