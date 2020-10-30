package com.richard.demo.jpa.datasource;

import com.richard.demo.datasource.DataSourceConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@AutoConfigureAfter(DataSourceConfiguration.class)
@Import(DynamicJpaRegistrar.class)
@Conditional(DynamicCondition.class)
@Configuration
public class DynamicJpaConfiguration {

    @Primary
    @Bean
    public JpaFactory jpaFactory() {
        return new JpaFactory();
    }
}
