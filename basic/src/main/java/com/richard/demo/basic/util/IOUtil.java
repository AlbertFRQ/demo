package com.richard.demo.basic.util;

import java.io.Closeable;

public class IOUtil {

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {

            }
        }
    }
}
