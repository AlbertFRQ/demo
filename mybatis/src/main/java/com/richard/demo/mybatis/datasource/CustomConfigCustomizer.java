package com.richard.demo.mybatis.datasource;

import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;

public class CustomConfigCustomizer implements ConfigurationCustomizer {

    @Override
    public void customize(Configuration configuration) {
        configuration.setCallSettersOnNulls(true);
    }
}
