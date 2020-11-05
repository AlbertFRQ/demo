package com.richard.demo.datasource.util;

import javax.sql.DataSource;

public class DataSourceUtil {

    @SuppressWarnings("unchecked")
    public static <T> T unwrap(DataSource dataSource, Class<T> tClass) {
        try {
            if (tClass.isInstance(dataSource)) {
                return (T) dataSource;
            }
            return dataSource.unwrap(tClass);
        } catch (Exception e) {
            return null;
        }
    }
}
