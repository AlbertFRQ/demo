package com.richard.demo.configuration.util;

import javax.sql.DataSource;

public class DsUtil {
    public static <T> T unwrapSilently(DataSource dataSource, Class<T> clazz) {
        try {
            if (clazz.isInstance(dataSource)) {
                return (T) dataSource;
            }
            return dataSource.unwrap(clazz);
        } catch (Exception e) {
            return null;
        }
    }
}
