package com.richard.demo.mybatis.datasource;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.ArrayList;
import java.util.List;

public class CustomConfigurationPostProcessor implements BeanPostProcessor {

    private final List<ConfigurationCustomizer> customizers;

    public CustomConfigurationPostProcessor(ObjectProvider<List<ConfigurationCustomizer>> customizers) {
        this.customizers = customizers.getIfAvailable(ArrayList::new);
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SqlSessionFactory) {
            SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) bean;
            customizers.forEach(configurationCustomizer -> configurationCustomizer.customize(sqlSessionFactory.getConfiguration()));
        }
        return bean;
    }
}
