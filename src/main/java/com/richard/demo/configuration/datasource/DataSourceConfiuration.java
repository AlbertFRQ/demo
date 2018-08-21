package com.richard.demo.configuration.datasource;

import com.richard.demo.configuration.util.DsUtil;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.boot.jdbc.metadata.HikariDataSourcePoolMetadata;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration("DataSourceConfiguration")
@EnableTransactionManagement
@Slf4j
public class DataSourceConfiuration {

    @Bean
    public BeanDefinitionRegistryPostProcessor beanDefinitionRegistryPostProcessor() {
        return new PrimaryDataSourceProcessor();
    }

    @Bean
    public BeanPostProcessor dataSourceProcessor() {
        return new DataSourceProcessor();
    }

    @Bean
    @Primary
    public DataSourcePoolMetadataProvider dataSourcePoolMetadataProvider() {
        return dataSource -> {
            HikariDataSource hikariDataSource = DsUtil.unwrapSilently(dataSource, HikariDataSource.class);
            return hikariDataSource == null ? null : new HikariDataSourcePoolMetadata(hikariDataSource);
        };
    }

    @Configuration
    @EnableDataSource("default")
    @EnableJpaRepositories(entityManagerFactoryRef = "default" + "EntityManagerFactory",
            transactionManagerRef = "default" + "TransactionManager",
            basePackages = "richard.datasource.default.repository")
    @ConditionalOnProperty(prefix = "richard.datasource." + "default", name = {"driverClassName", "url", "username", "password"})
    public static class DefaultDataSource {

    }
}
