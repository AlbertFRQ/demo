package com.richard.demo.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration("richardDataSourceConfiguration")
@Import(DataSourceRegistrar.class)
@EnableTransactionManagement
@Slf4j
public class DataSourceConfiguration {

    @Bean
    public static BeanDefinitionRegistryPostProcessor primaryDataSourceProcessor() {
        return new PrimaryDataSourceProcessor();
    }
}
