package com.richard.demo.mybatis.datasource;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.util.StringUtils;

public class CustomNameGenerator implements BeanNameGenerator {

    private String dataSourceName;

    CustomNameGenerator(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    @Override
    public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
        return dataSourceName + getBeanClassName(definition.getBeanClassName());
    }

    private String getBeanClassName(String name) {
        if (!StringUtils.hasText(name)) {
            return "";
        }
        return name.contains(".") ? name.substring(name.lastIndexOf(".") + 1) : name;
    }
}
