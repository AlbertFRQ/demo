package com.richard.demo.basic.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.SneakyThrows;
import org.springframework.cache.Cache;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ObjectUtil {

    private static final com.github.benmanes.caffeine.cache.Cache<String, Class> classCache =
            Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(2000).build();

    public static <T> T value(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    public static <T> T value(T value, Supplier<T> supplier) {
        return value != null ? value : supplier.get();
    }

    public static <T> T getCacheValue(Cache cache, String key) {
        return getCacheValue(cache, key, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getCacheValue(Cache cache, String key, T defaultValue) {
        if (isEmpty(cache) || isEmpty(key)) {
            return defaultValue;
        }
        Cache.ValueWrapper wrapper = cache.get(key);
        if (isEmpty(wrapper) || isEmpty(wrapper.get())) {
            return defaultValue;
        }
        return (T) wrapper.get();
    }

    public static boolean isEmpty(Object object) {
        if (object == null) {
            return true;
        }
        boolean res = false;
        if (object instanceof CharSequence) {
            res = !StringUtils.hasText((CharSequence) object);
        } else if (object instanceof Map) {
            res = ((Map) object).isEmpty();
        } else if (object instanceof Iterable) {
            if (object instanceof Collection) {
                res = ((Collection) object).isEmpty();
            } else {
                res = !((Iterable) object).iterator().hasNext();
            }
        } else if (object.getClass().isArray()) {
            res = Array.getLength(object) == 0;
        }
        return res;
    }

    @SneakyThrows
    public static String toJson(Object object) {
        if (isEmpty(object)) {
            return null;
        }
        Writer writer = null;
        try {
            writer = new StringWriter();
            getObjectMapper().writeValue(writer, object);
            return writer.toString();
        } finally {
            IOUtil.close(writer);
        }
    }

    @SneakyThrows
    public static String toPrettyJson(Object object) {
        if (isEmpty(object)) {
            return null;
        }
        Writer writer = null;
        try {
            writer = new StringWriter();
            getObjectMapper().writerWithDefaultPrettyPrinter()
                    .writeValue(writer, object);
            return writer.toString();
        } finally {
            IOUtil.close(writer);
        }
    }

    @SneakyThrows
    public static <T> T jsonToObject(String json, Class<T> target) {
        if (isEmpty(json)) {
            return null;
        }
        return getObjectMapper().readValue(json, target);
    }

    @SneakyThrows
    public static Map<String, Object> jsonToMap(String json) {
        if (isEmpty(json)) {
            return null;
        }
        TypeReference<LinkedHashMap<String, Object>> reference = new TypeReference<LinkedHashMap<String, Object>>(){};
        return getObjectMapper().readValue(json, reference);
    }

    @SneakyThrows
    public static Map<String, String> jsonToStringMap(String json) {
        if (isEmpty(json)) {
            return null;
        }
        TypeReference<LinkedHashMap<String, String>> reference = new TypeReference<LinkedHashMap<String, String>>(){};
        return getObjectMapper().readValue(json, reference);
    }

    public static ObjectMapper getObjectMapper() {
        return SpringContext.getBean(ObjectMapper.class);
    }

    public static Class<?> resolveClassName(String className) {
        return classCache.get(className, cn -> ClassUtils.resolveClassName(cn, null));
    }
}
