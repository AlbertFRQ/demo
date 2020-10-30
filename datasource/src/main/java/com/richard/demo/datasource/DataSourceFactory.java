package com.richard.demo.datasource;

import com.p6spy.engine.spy.P6DataSource;
import com.richard.demo.basic.util.SpringContext;
import com.richard.demo.datasource.log.HikariLogDataSource;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.Set;

@Slf4j
public class DataSourceFactory {

    public static Set<String> registerIfAbsent(BeanDefinitionRegistry beanDefinitionRegistry) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) beanDefinitionRegistry;
        Environment environment = beanFactory.getBean(Environment.class);

        Set<String> dataSourceNames = SpringContext.getConfigGroups(environment, "richard.datasource", "url");
        dataSourceNames.forEach(name -> {
            if (!beanFactory.containsBeanDefinition(name + "DataSource")) {
                log.info("Data Source registering, {}", name);
                DataSource dataSource = new P6DataSource(new HikariLogDataSource(createDelegate(name, environment)));

                beanFactory.registerBeanDefinition(name + "DataSource",
                        BeanDefinitionBuilder.rootBeanDefinition(DelegateDataSource.class)
                                .addConstructorArgValue(dataSource)
                                .getBeanDefinition());

                beanFactory.registerBeanDefinition(name + "JdbcTemplate",
                        BeanDefinitionBuilder.rootBeanDefinition(JdbcTemplate.class)
                                .addConstructorArgReference(name + "DataSource")
                                .getBeanDefinition());
            }
        });
        return dataSourceNames;
    }

    static HikariDataSource createDelegate(String name, Environment environment) {
        String configPrefix = "richard.datasource." + name + ".";

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName(name);
        dataSource.setJdbcUrl(SpringContext.getRequiredProperty(configPrefix, "url", environment));
        dataSource.setDriverClassName(SpringContext.getRequiredProperty(configPrefix, "driverClassName", environment));
        dataSource.setUsername(SpringContext.getRequiredProperty(configPrefix, "username", environment));
        dataSource.setPassword(SpringContext.getRequiredProperty(configPrefix, "password", environment));
        dataSource.setMaximumPoolSize(SpringContext.getIntProperty(configPrefix, "maxActive", environment, 10));
        dataSource.setConnectionTimeout(SpringContext.getIntProperty(configPrefix, "maxWait", environment, 20000));
        dataSource.setReadOnly(SpringContext.getBooleanProperty(configPrefix, "readonly", environment));

        dataSource.setIdleTimeout(SpringContext.getIntProperty(configPrefix, "minEvictableIdleTimeMillis", environment, 300000));
        dataSource.setMaxLifetime(SpringContext.getIntProperty(configPrefix, "maxAge", environment, 1800000));
        dataSource.setMinimumIdle(SpringContext.getIntProperty(configPrefix, "minIdle", environment, 0));
        dataSource.setConnectionTestQuery(SpringContext.getProperty(configPrefix, "validationQuery", environment));

        dataSource.setInitializationFailTimeout(-1);
        dataSource.setLeakDetectionThreshold(SpringContext.getIntProperty(configPrefix, "leakDetectionThresholdMillis", environment, 1800000));
        return dataSource;
    }
}
