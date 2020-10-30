package com.richard.demo.datasource.log;

import com.p6spy.engine.spy.appender.StdoutLogger;
import com.richard.demo.datasource.util.SqlFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class SqlLogger extends StdoutLogger {

    private static final Logger SQL_LOG = LoggerFactory.getLogger("SQL");
    private static final SqlFormatter FORMATTER = new SqlFormatter();

    public void logText(String text) {
        log(text);
    }

    public void logException(Exception e) {

    }

    public static void log(String text) {
        if (StringUtils.hasLength(text)) {
            SQL_LOG.info(FORMATTER.format(text));
        }
    }
}
