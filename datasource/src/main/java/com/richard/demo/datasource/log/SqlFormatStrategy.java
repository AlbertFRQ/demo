package com.richard.demo.datasource.log;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;

public class SqlFormatStrategy implements MessageFormattingStrategy {

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        return sql;
    }
}
