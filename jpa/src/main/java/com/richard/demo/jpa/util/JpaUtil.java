package com.richard.demo.jpa.util;

import javax.sql.DataSource;
import java.sql.SQLException;

public class JpaUtil {

    @SuppressWarnings("unchecked")
    public static <T> T unWrapper(DataSource dataSource, Class<T> clazz) {
        try {
            if (clazz.isInstance(dataSource)) {
                return (T) dataSource;
            }
            return dataSource.unwrap(clazz);
        } catch (SQLException e) {
            return null;
        }
    }
}
