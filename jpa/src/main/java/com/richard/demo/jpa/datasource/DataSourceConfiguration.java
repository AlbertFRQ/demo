package com.richard.demo.jpa.datasource;

import com.richard.demo.jpa.annotation.EnableDataSource;
import com.richard.demo.jpa.util.JpaUtil;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.boot.jdbc.metadata.HikariDataSourcePoolMetadata;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration("richardDataSourceConfiguration")
@Import(JpaRepositoriesAutoConfiguration.class)
@EnableTransactionManagement
@Slf4j
public class DataSourceConfiguration {

    @Primary
    @Bean
    public DataSourcePoolMetadataProvider hikariDataSourcePoolMetadataProvider() {
        return dataSource -> {
            HikariDataSource hikariDataSource = JpaUtil.unWrapper(dataSource, HikariDataSource.class);
            return hikariDataSource == null ? null : new HikariDataSourcePoolMetadata(hikariDataSource);
        };
    }

    @Bean
    public static BeanDefinitionRegistryPostProcessor primaryDataSourceProcessor() {
        return new PrimaryDataSourceProcessor();
    }

    @Bean
    public BeanPostProcessor dataSourcePostProcessor() {
        return new DataSourceProcessor();
    }

    @Primary
    @Configuration
    @EnableDataSource("default")
    @EnableJpaRepositories(entityManagerFactoryRef = "default" + "EntityManagerFactory",
            transactionManagerRef = "default" + "TransactionManager",
            basePackages = "${richard.datasource." + "default" + ".repository}")
    @ConditionalOnProperty(prefix = "richard.datasource." + "default", name = {"driver", "url", "username", "password"})
    protected static class defaultDataSourceConfiguration {

    }
}
